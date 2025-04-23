package org.c4marathon.assignment.domain.service.mainaccount;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferRetryService {


	private final MainAccountRepository mainAccountRepository;
	private final PlatformTransactionManager transactionManager;


	private static final int MAX_RETRY = 5; // 10
	private static final long BASE_SLEEP_TIME_MS = 100;      // 초기 대기 시간 // 50
	private static final long MAX_SLEEP_TIME_MS = 2000;     // 최대 대기 시간 // 10000


	private final EntityManager entityManager;

	// 멀티스레드 환경에서 동시성 문제 없이 다음을 수행하고 싶을 때:
	// AtomicInteger retrySuccessCounter = new AtomicInteger(0);
	// private final ConcurrentLinkedQueue<Integer> retryAttemptHistory = new ConcurrentLinkedQueue<>();


	/**
	 * 이체를 재시도 로직과 함께 수행
	 * 성능보다 정합성이 우선되어야함.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void transferWithRetry(Long fromAccountId, Long toAccountId, Long transferAmount) {
		MainAccount[] accounts = new MainAccount[2];

		executeWithRetry(() -> {
			// 재시도할 때마다 새로운 트랜잭션 컨텍스트에서 최신 엔티티 조회
			accounts[0] = getRefreshedAccount(fromAccountId);
			accounts[1] = getRefreshedAccount(toAccountId);

			// 새 트랜잭션에서 이체 수행
			return performTransferInNewTransaction(accounts[0], accounts[1], transferAmount);
		});
	}

	/**
	 * 재시도 로직을 통한 함수 실행
	 */
	// // 재시도 로직까지 하나의 트랜잭션으로 묶여서 예외가 터진것을 롤백할 상황이라고 생각
	// // 1. @Transactional 메서드 진입 → 트랜잭션 시작
	// // 2. 중간에 예외 발생 → rollback-only 플래그 설정
	// // 3. catch로 예외를 처리하더라도 rollback-only는 유지됨
	// // 4. 메서드 종료 후 커밋 시도 → 이미 rollback-only라서 UnexpectedRollbackException 발생
	private <T> void executeWithRetry(Callable<T> operation) {
		int attempts = 0;
		Exception lastException = null;

		while (attempts < MAX_RETRY) {
			try {
				operation.call();

				// if (attempts > 0) {
				// 	log.info("이체 처리 완료. 재시도 성공 {}/{}", attempts, MAX_RETRY);
				// 	retrySuccessCounter.incrementAndGet();
				// }
				// if (attempts > 1) {
				// 	retryAttemptHistory.add(attempts);
				// }
				return;

			} catch (Exception e) {
				if (isRetryableException(e)) {
					attempts++;
					lastException = e;

					if (attempts >= MAX_RETRY) {
						// log.error("최대 재시도 횟수 초과: {}", e.getMessage());
						break;
					}

					log.warn("이체 처리 중 오류 발생. 재시도 {}/{}: {}", attempts, MAX_RETRY, e.getMessage());
					sleepWithBackoff(attempts);

					// 엔티티 매니저 초기화 - 현재 컨텍스트에서 캐시된 엔티티 제거
					entityManager.clear();
				} else {
					throw new RuntimeException("재시도 불가능한 비즈니스 예외 발생", e);
				}
			}
		}

		throw new RuntimeException("최대 재시도 횟수 초과", lastException);
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

	/**
	 * 새로운 트랜잭션에서 이체 수행
	 */
	// 이전 트랜잭션의 rollback-only 상태를 회피하려는 의도 (원래 아래 트랜잭션으로 하려했지만, 이후 리트라이 로직으로 변경) (requrieds_new 삭제)
	// 상위 트랜잭션이 아직 종료되지 않아 락을 보유 중인 상태에서, 하위 메서드가 새로운 트랜잭션(REQUIRES_NEW) 으로 동일 자원에 접근하면서 락 충돌이 발생
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private Boolean performTransferInNewTransaction(MainAccount from, MainAccount to, Long amount) {
		// Spring AOP의 한계 때문에 직접 설정
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				// 출금
				withdraw(from, amount);
				// 입금
				deposit(to, amount);
				return true;
			} catch (Exception e) {
				status.setRollbackOnly();
				throw e;
			}
		});
	}

	private MainAccount getRefreshedAccount(Long accountId) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.setReadOnly(true);

		return template.execute(status ->
			mainAccountRepository.findById(accountId)
				.orElseThrow(() -> new IllegalStateException("ID가 " + accountId + "인 메인 계좌가 존재하지 않습니다."))
		);
	}

	private boolean isRetryableException(Exception e) {
		return e instanceof OptimisticLockException ||
			e instanceof DataAccessException ||
			e instanceof TransactionSystemException;
	}

	private void withdraw(MainAccount from, Long amount) {
		int withdrawResult = mainAccountRepository.withdrawByOptimistic(
			from.getId(),
			amount,
			from.getVersion()
		);

		if (withdrawResult == 0) {
			throw new OptimisticLockingFailureException("출금 처리 중 충돌이 발생했습니다.");
		}
	}

	private void deposit(MainAccount to, Long amount) {
		int depositResult = mainAccountRepository.depositByOptimistic(
			to.getId(),
			amount,
			to.getVersion()
		);

		if (depositResult == 0) {
			throw new OptimisticLockingFailureException("입금 처리 중 충돌이 발생했습니다.");
		}
	}
}
