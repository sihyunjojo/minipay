package org.c4marathon.assignment.domain.service.mainaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.common.exception.RetryableException;
import org.c4marathon.assignment.common.generator.AccountNumberGenerator;
import org.c4marathon.assignment.domain.model.account.enums.AccountType;
import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;

import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.infra.retry.AccountNumberRetryExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final MainAccountRepository mainAccountRepository;
	private final AccountPolicyProperties accountPolicyProperties;
	private final AccountNumberGenerator accountNumberGenerator;
	private final AccountNumberRetryExecutor accountNumberRetryExecutor;

	@Transactional
	public void createMainAccountForMember(Member member) {
		validateNoMainAccount(member);

		String accountNumber = generateUniqueAccountNumber();

		MainAccount mainAccount = MainAccount.create(member, accountNumber);
		member.setMainAccount(mainAccount);
		mainAccountRepository.save(mainAccount);
	}

	private void validateNoMainAccount(Member member) {
		boolean accountExists = mainAccountRepository.findByMemberId(member.getId()).isPresent();
		if (accountExists) {
			throw new IllegalStateException("회원이 이미 메인 계좌를 가지고 있습니다.");
		}
	}

	/**
	 * 고유한 메인 계좌 번호를 생성합니다.
	 */
	private String generateUniqueAccountNumber() {
		return accountNumberRetryExecutor.executeWithRetry(() -> {
			String candidate = accountNumberGenerator.generate(AccountType.MAIN_ACCOUNT);

			if (mainAccountRepository.existsByAccountNumber(candidate)) {
				throw new RetryableException("중복된 메인 계좌번호 발생. 재시도합니다.");
			}
			return candidate;
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public MainAccount getRefreshedAccount(Long accountId) {
		return mainAccountRepository.findByIdWithoutSecondCache(accountId)
			.orElseThrow(() -> new IllegalStateException(String.format("ID가 %s인 메인 계좌가 존재하지 않습니다.", accountId)));
	}

	@Transactional
	public void resetAllDailyChargeAmount() {
		mainAccountRepository.resetAllDailyChargeAmount();
	}

	public Long calculateShortfall(Long accountId, Long transferAmount) {
		Long currentBalance = mainAccountRepository.findMainAccountAmountById(accountId);
		long diff = transferAmount - currentBalance;

		return Math.max(diff, 0L);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void chargeOrThrow(Long accountId, Long chargeAmount, Long minRequiredBalance) {
		boolean success = mainAccountRepository.tryFastCharge(accountId, chargeAmount, minRequiredBalance,
			accountPolicyProperties.getMain().getMainDailyLimit());

		if (!success) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}

}
