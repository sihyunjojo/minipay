package org.c4marathon.assignment.usecase;

import java.util.List;

import org.c4marathon.assignment.domain.model.account.Account;
import org.c4marathon.assignment.domain.model.policy.ExternalAccountPolicy;
import org.c4marathon.assignment.domain.model.transfer.PendingTransferTransaction;
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

		Account toAccount = mainAccountService.getRefreshedAccount(request.fromAccountId());
		TransferLog transferLog = transferLogFactory.createExternalChargeLog(
			ExternalAccountPolicy.TEMPORARY_CHARGING,
			toAccount,
			chargeAmount);
		transferLogService.saveTransferLog(transferLog);

		PendingTransferTransaction tx = retryExecutor.executeWithRetry(
			() -> pendingTransferService.initiate(
				request.fromAccountId(),
				request.toAccountId(),
				request.amount()
			)
		);

		Account fromAccount = mainAccountService.getRefreshedAccount(request.fromAccountId());
		Account toAccount2 = mainAccountService.getRefreshedAccount(request.toAccountId());
		TransferLog immediateTransferLog = transferLogFactory.createPendingTransferLog(fromAccount,
				toAccount2, request.amount(), tx.getCreatedAt());
		transferLogService.saveTransferLog(immediateTransferLog);
	}

	@Transactional
	public void acceptPendingTransfer(Long transactionId) {
		// 대기 중인 거래 조회
		PendingTransferTransaction tx = pendingTransferService.findPendingPendingTransferTransaction(transactionId);

		// 거래 수락 실행
		retryExecutor.executeWithRetry(() -> pendingTransferService.accept(transactionId));

		// 거래 로그 생성 및 저장
		TransferLog transferLog = transferLogFactory.createCompletePendingTransferLog(
				tx.getId(),
				tx.getFromMainAccount(),
				tx.getToMainAccount(),
				tx.getAmount(),
				tx.getCreatedAt());
		transferLogService.saveTransferLog(transferLog);
	}

	@Transactional
	public void cancelPendingTransfer(Long transactionId) {
		// 대기 중인 거래 조회
		PendingTransferTransaction tx = pendingTransferService.findPendingPendingTransferTransaction(transactionId);

		// 거래 취소 실행
		retryExecutor.executeWithRetry(() -> pendingTransferService.cancel(transactionId));

		// 거래 로그 생성 및 저장
		TransferLog transferLog = transferLogFactory.createCancelPendingTransferLog(
				tx.getId(),
				tx.getFromMainAccount(),
				tx.getToMainAccount(),
				tx.getAmount(),
				tx.getCreatedAt());
		transferLogService.saveTransferLog(transferLog);
	}

	@Transactional
	public void expirePendingTransfer() {
		List<PendingTransferTransaction> expiredPendingPendingTransferTransactions = pendingTransferService
				.findAllByExpiredPendingPendingTransferTransactionWithMainAccount();
		for (PendingTransferTransaction tx : expiredPendingPendingTransferTransactions) {
			pendingTransferService.expired(tx);

			TransferLog transferLog = transferLogFactory.createExpirePendingTransferLog(
					tx.getId(),
					tx.getFromMainAccount(),
					tx.getToMainAccount(),
					tx.getAmount(),
					tx.getCreatedAt());
			transferLogService.saveTransferLog(transferLog);
		}
	}

}
