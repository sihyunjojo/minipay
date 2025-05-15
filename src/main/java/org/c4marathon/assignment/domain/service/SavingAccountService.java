package org.c4marathon.assignment.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.common.exception.RetryableException;
import org.c4marathon.assignment.common.generator.AccountNumberGenerator;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.c4marathon.assignment.domain.model.account.enums.AccountType;
import org.c4marathon.assignment.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.c4marathon.assignment.dto.account.AccountResponseDto;
import org.c4marathon.assignment.dto.account.CreateFixedSavingAccountRequestDto;
import org.c4marathon.assignment.dto.account.SavingDepositRequest;
import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.infra.retry.AccountNumberRetryExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

	private final AccountPolicyProperties accountPolicyProperties;
	private final SavingAccountRepository savingAccountRepository;
	private final MainAccountRepository mainAccountRepository;
	private final AccountNumberGenerator accountNumberGenerator;
	private final AccountNumberRetryExecutor accountNumberRetryExecutor;

	@Transactional
	public AccountResponseDto createFixedSavingAccount(Long memberId, CreateFixedSavingAccountRequestDto request) {
		MainAccount mainAccount = mainAccountRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalArgumentException("메인 계좌를 찾을 수 없습니다."));

		String accountNumber = generateUniqueAccountNumber();

		SavingAccount savingAccount = SavingAccount.createFixed(
			accountNumber,
			mainAccount.getMember(),
			mainAccount,
			request.subscribedDepositAmount()
		);

		savingAccountRepository.save(savingAccount);
		return new AccountResponseDto(savingAccount);
	}

	@Transactional
	public AccountResponseDto createFlexibleSavingAccount(Long memberId) {
		MainAccount mainAccount = mainAccountRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalArgumentException("메인 계좌를 찾을 수 없습니다."));

		String accountNumber = generateUniqueAccountNumber();

		SavingAccount savingAccount = SavingAccount.createFlexible(
			accountNumber,
			mainAccount.getMember(),
			mainAccount
		);

		savingAccountRepository.save(savingAccount);
		return new AccountResponseDto(savingAccount);
	}

	private String generateUniqueAccountNumber() {
		return accountNumberRetryExecutor.executeWithRetry(() -> {
			String candidate = accountNumberGenerator.generate(AccountType.SAVING_ACCOUNT);

			if (savingAccountRepository.existsByAccountNumber(candidate)) {
				throw new RetryableException("중복된 적금 계좌번호 발생. 재시도합니다.");
			}
			return candidate;
		});
	}

	@Transactional(readOnly = true)
	public SavingAccount findByAccountNumberOrThrow(String accountNumber) {
		return savingAccountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new IllegalStateException(String.format("계좌번호 %s인 적금 계좌가 존재하지 않습니다.", accountNumber)));
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public SavingAccount getRefreshedAccount(Long accountId) {
		return savingAccountRepository.findByIdWithoutSecondCache(accountId)
			.orElseThrow(() -> new IllegalStateException(String.format("ID가 %s인 적금 계좌가 존재하지 않습니다.", accountId)));
	}

	@Transactional
	public void deposit(Long accountId, Long amount) {
		SavingAccount account = savingAccountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없음"));
		account.deposit(amount);
	}

	@Transactional(readOnly = true)
	public Long getMainAccountId(Long savingAccountId) {
		SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
			.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없음"));
		return savingAccount.getMainAccount().getId();
	}

	@Transactional
	public void applyInterest() {
		List<SavingAccount> accounts = savingAccountRepository.findAll();
		for (SavingAccount account : accounts) {
			// fixme: 금액 계산은 BigDecimal 사용을 고려
			double rate = accountPolicyProperties.getSaving().getInterestRate(account.getSavingType());
			Long interest = account.calculateInterest(rate);
			account.deposit(interest);
		}
	}

	public Map<MainAccount, List<SavingDepositRequest>> getSubscribedDepositAmount() {
		List<SavingAccount> accounts = savingAccountRepository.findAllFixedSavingAccountWithMemberAndMainAccount();

		return accounts.stream()
			.filter(this::isLinkedToMainAccount)
			.collect(Collectors.groupingBy(
				account -> account.getMember().getMainAccount(),
				Collectors.mapping(
					account -> new SavingDepositRequest(account, account.getSubscribedDepositAmount()),
					Collectors.toList()
				)
			));
	}

	private boolean isLinkedToMainAccount(SavingAccount account) {
		return account.getMember() != null && account.getMember().getMainAccount() != null;
	}
}

