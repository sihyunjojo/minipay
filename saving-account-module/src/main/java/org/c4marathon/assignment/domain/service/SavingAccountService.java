package org.c4marathon.assignment.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.AccountNumberRetryExecutor;
import org.c4marathon.assignment.domain.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

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

	@Transactional(readOnly = true)
	public List<SavingAccount> findAll(){
		return savingAccountRepository.findAll();
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

	@Transactional(readOnly = true)
	public Long getMainAccountId(Long savingAccountId) {
		SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
			.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없음"));
		return savingAccount.getMainAccount().getId();
	}

	@Transactional
	public Long applyInterest(SavingAccount account) {
		double rate = savingAccountPolicy.getInterestRate(account.getSavingType());
		Long interest = account.calculateInterest(rate);
		account.deposit(interest);
		return interest;
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

