package org.c4marathon.assignment.scheduler;

import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.c4marathon.assignment.domain.service.ReminderService;
import org.c4marathon.assignment.domain.service.TransferTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingTransferScheduler {

	private final TransferTransactionService transferTransactionService;
	private final ReminderService reminderService;

	// Todo: fcm 알림 붙이기.
	@Scheduled(fixedDelay = 600000)
	@Transactional
	public void remindPendingTargetTransactions() {
		try {
			List<TransferTransaction> remindPendingTargetTransactions = transferTransactionService.findRemindPendingTargetTransactionsWithMember();
			for (TransferTransaction transferTransaction : remindPendingTargetTransactions) {
				reminderService.remindTransactions(transferTransaction);
			}
			log.info("모든 사용자에게 알림 완료");
		} catch (Exception e) {
			log.error("대기 중인 송금에 대한 알림 중 오류 발생", e);
		}
	}

	@Scheduled(fixedDelay = 600000)
	@Transactional
	public void remindPendingTargetTransactionsByImproved() {
		try {
			Map<Member, List<TransferTransaction>> remindTargetGroupedByMember = transferTransactionService.findRemindTargetGroupedByMember();
			reminderService.remindTransactions(remindTargetGroupedByMember);
			log.info("모든 사용자에게 알림 완료");
		} catch (Exception e) {
			log.error("대기 중인 송금에 대한 알림 중 오류 발생", e);
		}
	}

	// Todo: 배치 처리 또는 chunk 단위 업데이트, 혹은 비동기 처리 , 재시도 전략
	@Scheduled(fixedDelay = 600000)
	@Transactional
	public void expireTargetTransactions() {
		try {
			List<TransferTransaction> expiredPendingTransferTransactions = transferTransactionService.findAllByExpiredPendingTransferTransactionWithMainAccount();
			for (TransferTransaction transferTransaction : expiredPendingTransferTransactions) {
				transferTransactionService.expired(transferTransaction);
				log.info("{}에 대한 환불 완료", transferTransaction.getId());
			}
			log.info("시간이 지난 대기 중이던 트랜잭션 만료 완료");
		} catch (Exception e) {
			log.error("시간이 지난 대기 중이던 트랜잭션 만료 중 오류 발생", e);
		}
	}
}
