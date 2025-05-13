package org.c4marathon.assignment.usecase;

import static org.c4marathon.assignment.domain.model.enums.PolicyType.*;

import java.util.List;

import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.c4marathon.assignment.domain.model.transferlog.TransferLogFactory;
import org.c4marathon.assignment.domain.service.PendingTransferService;
import org.c4marathon.assignment.domain.service.TransferLogService;
import org.c4marathon.assignment.dto.transfer.TransferPendingRequestDto;
import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingTransferUseCase {

	private final MainAccountService mainAccountService;
	private final PendingTransferService pendingTransferService;
	private final TransferLogService transferLogService;
	private final TransferLogFactory transferLogFactory;

	private final AccountPolicyProperties accountPolicyProperties;
	private final RetryExecutor retryExecutor;

	@Transactional
	public void createPendingTransfer(TransferPendingRequestDto request) {
		Long shortfall = mainAccountService.calculateShortfall(request.fromAccountId(), request.amount());

		if (shortfall <= 0) {
			retryExecutor.executeWithRetry(
					() -> pendingTransferService.initiate(request.fromAccountId(), request.toAccountId(),
							request.amount()));
			return;
		}

		long chargeAmount = accountPolicyProperties.getMain().getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(request.fromAccountId(), chargeAmount, request.amount());

		TransferLog transferLog = transferLogFactory.createExternalChargeLog(
			TEMPORARY_CHARGING_ID.getValue(),			request.fromAccountId(),
			chargeAmount);
		transferLogService.saveTransferLog(transferLog);

		retryExecutor.executeWithRetry(
				() -> pendingTransferService.initiate(request.fromAccountId(), request.toAccountId(),
						request.amount()));

		TransferLog immediateTransferLog = transferLogFactory.createPendingTransferLog(request.fromAccountId(),
				request.toAccountId(), request.amount());
		transferLogService.saveTransferLog(immediateTransferLog);
	}

	@Transactional
	public void acceptPendingTransfer(Long transactionId) {
		// 대기 중인 거래 조회
		TransferTransaction tx = pendingTransferService.findPendingTransferTransaction(transactionId);

		// 거래 수락 실행
		retryExecutor.executeWithRetry(() -> pendingTransferService.accept(transactionId));

		// 거래 로그 생성 및 저장
		TransferLog transferLog = transferLogFactory.createCompletePendingTransferLog(
				tx.getId(),
				tx.getFromMainAccount().getId(),
				tx.getToMainAccount().getId(),
				tx.getAmount());
		transferLogService.saveTransferLog(transferLog);
	}

	@Transactional
	public void cancelPendingTransfer(Long transactionId) {
		// 대기 중인 거래 조회
		TransferTransaction tx = pendingTransferService.findPendingTransferTransaction(transactionId);

		// 거래 취소 실행
		retryExecutor.executeWithRetry(() -> pendingTransferService.cancel(transactionId));

		// 거래 로그 생성 및 저장
		TransferLog transferLog = transferLogFactory.createCancelPendingTransferLog(
				tx.getId(),
				tx.getFromMainAccount().getId(),
				tx.getToMainAccount().getId(),
				tx.getAmount());
		transferLogService.saveTransferLog(transferLog);
	}

	@Transactional
	public void expirePendingTransfer() {
		List<TransferTransaction> expiredPendingTransferTransactions = pendingTransferService
				.findAllByExpiredPendingTransferTransactionWithMainAccount();
		for (TransferTransaction tx : expiredPendingTransferTransactions) {
			pendingTransferService.expired(tx);

			TransferLog transferLog = transferLogFactory.createExpirePendingTransferLog(
					tx.getId(),
					tx.getFromMainAccount().getId(),
					tx.getToMainAccount().getId(),
					tx.getAmount());
			transferLogService.saveTransferLog(transferLog);
		}
	}

}
