package org.c4marathon.assignment.domain.service.mainaccount;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.enums.AccountPolicy;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.c4marathon.assignment.domain.service.AccountPolicyService;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainAccountService {

	private static final int MAX_RETRY = 5; // 10
	private static final long BASE_SLEEP_TIME_MS = 100;      // 초기 대기 시간 // 50
	private static final long MAX_SLEEP_TIME_MS = 2000;     // 최대 대기 시간 // 10000

	private static final List<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = List.of(OptimisticLockException.class,
		ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class,
		LockTimeoutException.class, MySQLTransactionRollbackException.class);

	private final EntityManager entityManager;

	private final MainAccountRepository mainAccountRepository;
	private final AccountPolicyService accountPolicyService;

	@Transactional
	public void createMainAccountForMember(Member member) {
		boolean accountExists = mainAccountRepository.findByMemberId(member.getId()).isPresent();
		if (accountExists) {
			throw new IllegalStateException("회원이 이미 메인 계좌를 가지고 있습니다.");
		}

		MainAccount mainAccount = MainAccount.builder().balance(0L).build();

		member.setMainAccount(mainAccount);
		mainAccountRepository.save(mainAccount);
	}

	// @Transactional(readOnly = true)를 쓰면 무조건 빨라진다고 오해하지만,
	// 실제로는 JPA의 변경 감지(dirty checking)를 비활성화하는 정도
	@Transactional(readOnly = true)
	public Long getMainAccountByMemberId(Long memberId) {
		return mainAccountRepository.findIdByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}



	@Transactional
	public void resetAllDailyChargeAmount() {
		mainAccountRepository.resetAllDailyChargeAmount();
	}

	@Transactional(readOnly = true)
	public Long calculateShortfall(Long accountId, Long transferAmount) {
		Long currentBalance = mainAccountRepository.findMainAccountAmountById(accountId);

		long diff = transferAmount - currentBalance;
		return Math.max(diff, 0L);    // 부족분이 없으면 0 반환
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void chargeOrThrow(Long accountId, Long shortfall, Long minRequiredBalance) {
		// Assert in DB (DB 정합성 확인하며 충전 시도)
		long chargeAmount = accountPolicyService.getRoundedCharge(shortfall);

		boolean success = mainAccountRepository.tryFastCharge(accountId, chargeAmount, minRequiredBalance,
			accountPolicyService.getPolicyValue(AccountPolicy.MAIN_DAILY_LIMIT));

		if (!success) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}

	public void validateTransfer(Long fromId, Long toId, Long amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
		if (fromId.equals(toId))
			throw new IllegalArgumentException("자신에게 송금할 수 없습니다.");
	}
}
