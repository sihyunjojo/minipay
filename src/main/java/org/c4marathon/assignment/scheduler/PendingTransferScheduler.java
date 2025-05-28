package org.c4marathon.assignment.scheduler;

import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.transfer.PendingTransferTransaction;
import org.c4marathon.assignment.domain.service.PendingTransferService;
import org.c4marathon.assignment.domain.service.ReminderService;
import org.c4marathon.assignment.usecase.PendingTransferUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingTransferScheduler {

	private final PendingTransferService pendingTransferService;
	private final PendingTransferUseCase pendingTransferUseCase;
	private final ReminderService reminderService;

	// Todo: fcm 알림 붙이기.
	// @Scheduled(fixedDelay = 6000)
	// @Transactional
	// public void remindPendingTargetTransactions() {
	// 	try {
	// 		List<TransferTransaction> remindPendingTargetTransactions = pendingTransferService.findRemindPendingTargetTransactionsWithMember();
	// 		for (TransferTransaction transferTransaction : remindPendingTargetTransactions) {
	// 			reminderService.remindTransactions(transferTransaction);
	// 		}
	// 	} catch (Exception e) {
	// 		log.error("대기 중인 송금에 대한 알림 중 오류 발생", e);
	// 	}
	// }

	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void remindPendingTargetTransactionsByImproved() {
		try {
			Map<Member, List<PendingTransferTransaction>> remindTargetGroupedByMember = pendingTransferService.findRemindTargetGroupedByMember();
			reminderService.remindTransactions(remindTargetGroupedByMember);
		} catch (Exception e) {
			log.error("[Good] 대기 중인 송금에 대한 알림 중 오류 발생", e);
		}
	}

	// Todo: 배치 처리 또는 chunk 단위 업데이트, 혹은 비동기 처리 , 재시도 전략
	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void expireTargetTransactions() {
		try {
			pendingTransferUseCase.expirePendingTransfer();
			log.info("시간이 지난 대기 중이던 트랜잭션 만료 완료");
		} catch (Exception e) {
			log.error("시간이 지난 대기 중이던 트랜잭션 만료 중 오류 발생", e);
		}
	}
}
