package org.c4marathon.assignment.domain.service.mainaccount;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.c4marathon.assignment.domain.repository.TransferTransactionRepository;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {

	private final PlatformTransactionManager transactionManager;

	private final TransferTransactionRepository transferTransactionRepository;
	private final MainAccountRepository mainAccountRepository;

	/**
	 * 새로운 트랜잭션에서 이체 수행
	 */
	// 이전 트랜잭션의 rollback-only 상태를 회피하려는 의도 (원래 아래 트랜잭션으로 하려했지만, 이후 리트라이 로직으로 변경) (requrieds_new 삭제)
	// 상위 트랜잭션이 아직 종료되지 않아 락을 보유 중인 상태에서, 하위 메서드가 새로운 트랜잭션(REQUIRES_NEW) 으로 동일 자원에 접근하면서 락 충돌이 발생
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	public Boolean transferInNewTransaction(MainAccount from, MainAccount to, Long amount) {
		// Spring AOP의 한계 때문에 직접 설정
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				// 출금
				withdraw(from, amount);
				// 입금
				deposit(to, amount);
				// 성공 → COMMIT됨
				return true;
			} catch (Exception e) {
				// 여기서 예외가 터지면 상위 트랜잭션에게 롤백을 무조건 하라고 명령
				// TransactionTemplate은 내부적으로 RuntimeException이나 Error가 아닌 예외가 던져지면 rollback을 안 합니다.
				// 그래서 catch 안에서 rollback을 명시적으로 강제해야 예외가 생겼을 때 rollback 되죠.
				status.setRollbackOnly();
				throw e;
			}
		});
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

	/**
	 * 새로운 트랜잭션에서 이체 시작 수행
	 */
	// @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Boolean initiate(Long fromAccountId, Long toAccountId, Long amount) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				MainAccount fromAccount = mainAccountRepository.findByIdWithSentTransactions(fromAccountId)
					.orElseThrow();
				int result = mainAccountRepository.withdrawByOptimistic(
					fromAccount.getId(),
					amount,
					fromAccount.getVersion()
				);

				if (result == 0) {
					throw new OptimisticLockException("잔고 출금 실패 - 동시성 문제");
				}

				MainAccount toAccount = mainAccountRepository.findByIdWithSentTransactions(toAccountId)
					.orElseThrow();
				TransferTransaction tx = TransferTransaction.createPending(fromAccount, toAccount, amount);

				transferTransactionRepository.save(tx);

				return true;
			} catch (Exception e) {
				status.setRollbackOnly();
				throw e;
			}
		});
	}

	/**
	 * 새로운 트랜잭션에서 이체 수락 수행
	 */
	public Boolean accept(Long transactionId) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				// 거래 조회 및 상태 확인
				TransferTransaction tx = transferTransactionRepository.findPendingTransferTransactionById(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("거래가 존재하지 않음"));

				// 입금 계좌 정보 갱신
				MainAccount toAccount = mainAccountRepository.findById(tx.getToMainAccount().getId())
					.orElseThrow(() -> new IllegalArgumentException("입금 계좌가 존재하지 않습니다"));

				// 입금 처리
				int result = mainAccountRepository.depositByOptimistic(
					tx.getToMainAccount().getId(),
					tx.getAmount(),
					toAccount.getVersion()
				);

				if (result == 0) {
					throw new OptimisticLockException("입금 실패 - 동시성 문제");
				}

				// 거래 완료 처리
				tx.markAsCompleted();
				transferTransactionRepository.save(tx);

				return true;
			} catch (Exception e) {
				status.setRollbackOnly();
				throw e;
			}
		});
	}

	/**
	 * 새로운 트랜잭션에서 이체 취소 수행
	 */
	public Boolean cancel(Long transactionId) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				// 거래 조회 및 상태 확인
				TransferTransaction tx = transferTransactionRepository.findPendingTransferTransactionById(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("대기 중인 거래가 존재하지 않음"));

				// 환불 계좌 정보 갱신
				MainAccount fromAccount = mainAccountRepository.findById(tx.getFromMainAccount().getId())
					.orElseThrow(() -> new IllegalArgumentException("환불 계좌가 존재하지 않습니다"));

				// 환불 처리
				int result = mainAccountRepository.depositByOptimistic(
					tx.getFromMainAccount().getId(),
					tx.getAmount(),
					fromAccount.getVersion()
				);

				if (result == 0) {
					throw new OptimisticLockException("환불 실패 - 동시성 문제");
				}

				// 거래 취소 처리
				tx.markAsCanceled();
				transferTransactionRepository.save(tx);

				return true;
			} catch (Exception e) {
				status.setRollbackOnly();
				throw e;
			}
		});
	}

	public Boolean expired(Long transactionId) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		return template.execute(status -> {
			try {
				// 거래 조회 및 상태 확인
				TransferTransaction tx = transferTransactionRepository.findPendingTransferTransactionById(transactionId)
					.orElseThrow(() -> new IllegalArgumentException("대기 중인 거래가 존재하지 않음"));

				// 환불 계좌 정보 갱신
				MainAccount fromAccount = mainAccountRepository.findById(tx.getFromMainAccount().getId())
					.orElseThrow(() -> new IllegalArgumentException("환불 계좌가 존재하지 않습니다"));

				// 환불 처리
				int result = mainAccountRepository.depositByOptimistic(
					tx.getFromMainAccount().getId(),
					tx.getAmount(),
					fromAccount.getVersion()
				);

				if (result == 0) {
					throw new OptimisticLockException("환불 실패 - 동시성 문제");
				}

				// 거래 취소 처리
				tx.markAsExpired();
				transferTransactionRepository.save(tx);

				return true;
			} catch (Exception e) {
				status.setRollbackOnly();
				throw e;
			}
		});
	}
}
