package org.c4marathon.assignment.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.infra.config.property.TransferTransactionPolicyProperties;
import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.c4marathon.assignment.domain.repository.transfertransaction.TransferTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferTransactionService {

	private final PlatformTransactionManager transactionManager;
	private final TransferTransactionPolicyProperties transferTransactionPolicyProperties;

	private final TransferTransactionRepository transferTransactionRepository;
	private final MainAccountRepository mainAccountRepository;

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
				int result = mainAccountRepository.withdrawByOptimistic(fromAccount.getId(), amount,
					fromAccount.getVersion());

				if (result == 0) {
					throw new OptimisticLockException("잔고 출금 실패 - 동시성 문제");
				}

				MainAccount toAccount = mainAccountRepository.findByIdWithSentTransactions(toAccountId).orElseThrow();
				TransferTransaction tx = TransferTransaction.createPending(fromAccount, toAccount, amount,
					transferTransactionPolicyProperties.getPendingTransferExpireAfterDuration());

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
					.orElseThrow(() -> new IllegalArgumentException("대기 중인 거래가 존재하지 않음"));

				// 입금 계좌 정보 갱신
				MainAccount toAccount = mainAccountRepository.findById(tx.getToMainAccount().getId())
					.orElseThrow(() -> new IllegalArgumentException("입금 계좌가 존재하지 않습니다"));

				// 입금 처리
				int result = mainAccountRepository.depositByOptimistic(tx.getToMainAccount().getId(), tx.getAmount(),
					toAccount.getVersion());

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
				int result = mainAccountRepository.depositByOptimistic(tx.getFromMainAccount().getId(), tx.getAmount(),
					fromAccount.getVersion());

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

	public void expired(TransferTransaction tx) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		template.execute(status -> {
			try {
				// 환불 계좌 정보 갱신
				MainAccount fromAccount = mainAccountRepository.findById(tx.getFromMainAccount().getId())
					.orElseThrow(() -> new IllegalArgumentException("환불 계좌가 존재하지 않습니다"));

				// 환불 처리
				int result = mainAccountRepository.depositByOptimistic(tx.getFromMainAccount().getId(), tx.getAmount(),
					fromAccount.getVersion());

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

	public Map<Member, List<TransferTransaction>> findRemindTargetGroupedByMember() {
		Duration duration = transferTransactionPolicyProperties.getPendingTransferRemindDuration();
		LocalDateTime remindTime = LocalDateTime.now().minus(duration);

		return transferTransactionRepository.findRemindTargetGroupedByMember(remindTime);
	}

	public List<TransferTransaction> findRemindPendingTargetTransactionsWithMember() {
		Duration duration = transferTransactionPolicyProperties.getPendingTransferRemindDuration();
		LocalDateTime remindTime = LocalDateTime.now().minus(duration);

		return transferTransactionRepository.findRemindPendingTargetTransactionsWithMember(remindTime);
	}

	public List<TransferTransaction> findAllByExpiredPendingTransferTransactionWithMainAccount() {
		return transferTransactionRepository.findRemindPendingTargetTransactionsWithMainAccount(LocalDateTime.now());
	}
}
