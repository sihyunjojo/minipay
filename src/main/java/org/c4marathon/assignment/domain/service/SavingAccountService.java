package org.c4marathon.assignment.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.c4marathon.assignment.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.dto.account.AccountResponseDto;
import org.c4marathon.assignment.dto.account.CreateFixedSavingAccountRequestDto;
import org.c4marathon.assignment.dto.account.SavingDepositRequest;
import org.c4marathon.assignment.infra.config.property.SavingAccountPolicyProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

	private final SavingAccountPolicyProperties savingAccountPolicyProperties;

	private final SavingAccountRepository savingAccountRepository;

	@Transactional
	public AccountResponseDto createFixedSavingAccount(MainAccount mainAccount, CreateFixedSavingAccountRequestDto request) {
		SavingAccount savingAccount = SavingAccount.createFixedSavingAccount(mainAccount.getMember(), mainAccount, request.subscribedDepositAmount());
		savingAccountRepository.save(savingAccount);

		return new AccountResponseDto(savingAccount);
	}

	@Transactional
	public AccountResponseDto createFlexibleSavingAccount(MainAccount mainAccount) {
		SavingAccount savingAccount = SavingAccount.createFlexibleSavingAccount(mainAccount.getMember(), mainAccount);
		savingAccountRepository.save(savingAccount);

		return new AccountResponseDto(savingAccount);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public SavingAccount getRefreshedAccount(Long accountId) {
		return savingAccountRepository.findByIdWithoutSecondCache(accountId)
			.orElseThrow(() -> new IllegalStateException(String.format("ID가 %s인 메인 계좌가 존재하지 않습니다.", accountId)));
	}

	public void deposit(Long accountId, Long amount) {
		SavingAccount account = savingAccountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없음"));
		account.deposit(amount);
	}

	@Transactional
	public void applyInterest() {
		List<SavingAccount> accounts = savingAccountRepository.findAll();
		for (SavingAccount account : accounts) {
			double rate = savingAccountPolicyProperties.getInterestRate(account.getSavingType());
			Long interest = account.calculateInterest(rate);
			account.deposit(interest);
		}
	}

	public Map<MainAccount, List<SavingDepositRequest>> getSubscribedDepositAmount() {
		List<SavingAccount> accounts = savingAccountRepository.findAllFixedSavingAccount();

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
