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
import org.c4marathon.assignment.dto.RetryResult;

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

	// 📌 2. ID 기반 최신 조회 (Post-fetch용)
	public MainAccount findById(Long id) {
		return mainAccountRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("ID가 " + id + "인 메인 계좌가 존재하지 않습니다."));
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

	@Transactional()
	public void chargeOrThrow(Long accountId, Long shortfall, Long minRequiredBalance) {
		// Assert in DB (DB 정합성 확인하며 충전 시도)
		long chargeAmount = accountPolicyService.getRoundedCharge(shortfall);

		boolean success = mainAccountRepository.tryFastCharge(accountId, chargeAmount, minRequiredBalance,
			accountPolicyService.getPolicyValue(AccountPolicy.MAIN_DAILY_LIMIT));

		if (!success) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}

	// 성능보다 정합성이 우선되어야함.
	// 재전송 로직을 통한
	public void transfer(Long fromAccountId, Long toAccountId, Long amount) {
		validateTransfer(fromAccountId, toAccountId, amount);

		MainAccount from = findById(fromAccountId);
		MainAccount to = findById(toAccountId);

		RetryResult<Void> withdrawResult = withdrawWithRetry(fromAccountId, amount, from);
		RetryResult<Void> depositResult = depositWithRetry(toAccountId, amount, to);

		if (withdrawResult.getRetryCount() > 0 || depositResult.getRetryCount() > 0) {
			log.info("출금 최종 재시도 횟수: {}", withdrawResult.getRetryCount());
			log.info("입금 최종 재시도 횟수: {}", depositResult.getRetryCount());
		}
	}

	private RetryResult<Void> withdrawWithRetry(Long accountId, Long amount, MainAccount initialAccount) {
		MainAccount[] holder = new MainAccount[] {initialAccount};

		return runWithRetry(() -> {
			tryWithdraw(accountId, amount, holder[0].getVersion());
			return null; // ✅ Void 타입이라 return null 필요
		}, () -> {
			entityManager.detach(holder[0]);
			holder[0] = findById(accountId);
			return holder[0];
		});
	}

	private RetryResult<Void> depositWithRetry(Long accountId, Long amount, MainAccount initialAccount) {
		MainAccount[] holder = new MainAccount[] {initialAccount};

		return runWithRetry(() -> {
			tryDeposit(accountId, amount, holder[0].getVersion());
			return null; // ✅ Void 타입이라 return null 필요
		}, () -> {
			entityManager.detach(holder[0]);
			holder[0] = findById(accountId);
			return holder[0];
		});
	}

	// 이전 트랜잭션의 rollback-only 상태를 회피하려는 의도
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void tryWithdraw(Long accountId, Long amount, Long version) {
		if (mainAccountRepository.withdrawByOptimistic(accountId, amount, version) <= 0) {
			throw new OptimisticLockException("동시성 충돌로 인해 출금 쿼리가 적용되지 않음");
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void tryDeposit(Long accountId, Long amount, Long version) {
		if (mainAccountRepository.depositByOptimistic(accountId, amount, version) <= 0) {
			throw new OptimisticLockException("동시성 충돌로 인해 입금 쿼리가 적용되지 않음");
		}
	}

	private void validateTransfer(Long fromId, Long toId, Long amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
		if (fromId.equals(toId))
			throw new IllegalArgumentException("자신에게 송금할 수 없습니다.");
	}

	// 재시도 로직까지 하나의 트랜잭션으로 묶여서 예외가 터진것을 롤백할 상황이라고 생각
	// 1. @Transactional 메서드 진입 → 트랜잭션 시작
	// 2. 중간에 예외 발생 → rollback-only 플래그 설정
	// 3. catch로 예외를 처리하더라도 rollback-only는 유지됨
	// 4. 메서드 종료 후 커밋 시도 → 이미 rollback-only라서 UnexpectedRollbackException 발생
	private <T> RetryResult<T> runWithRetry(Callable<T> operation, Supplier<MainAccount> retryLoader) {
		int retryCount = 0;

		while (true) {
			try {
				T result = operation.call(); // 💡 성공 시
				if (retryCount > 0) {
					log.debug("시도 완료: retryCount = {}", retryCount);
				}
				return new RetryResult<>(result, retryCount); // 💡 여기에 retryCount 담김
			} catch (Throwable e) {
				retryCount++;
				if (retryCount >= MAX_RETRY || !isRetryable(e)) {
					log.error("최대 재시도 횟수 초과 또는 재시도 불가능한 예외: {}", e.getMessage());
					throw new RuntimeException(e);
				}
				sleepWithBackoff(retryCount);
				retryLoader.get();
			}
		}
	}

	private boolean isRetryable(Throwable ex) {
		return RETRYABLE_EXCEPTIONS.stream().anyMatch(clazz -> clazz.isAssignableFrom(ex.getClass()));
	}

	private void sleepWithBackoff(int retryCount) {
		try {
			long sleep = Math.min(BASE_SLEEP_TIME_MS * (1L << retryCount), MAX_SLEEP_TIME_MS);
			sleep += ThreadLocalRandom.current().nextLong(0, 100); // jitter
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("스레드 인터럽트 발생", e);
		}
	}
}
