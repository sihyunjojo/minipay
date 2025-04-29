package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.PendingTransferService;
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

	private final AccountPolicyProperties accountPolicyProperties;
	private final RetryExecutor retryExecutor;

	@Transactional
	public void initiatePendingTransfer(TransferPendingRequestDto request) {
		Long shortfall = mainAccountService.calculateShortfall(request.fromAccountId(), request.amount());

		if (shortfall <= 0) {
			retryExecutor.executeWithRetry(
				() -> pendingTransferService.initiate(request.fromAccountId(), request.toAccountId(), request.amount()));
			return;
		}

		long chargeAmount = accountPolicyProperties.getMain().getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(request.fromAccountId(), chargeAmount, request.amount());

		retryExecutor.executeWithRetry(
			() -> pendingTransferService.initiate(request.fromAccountId(), request.toAccountId(), request.amount()));
	}

	@Transactional
	public void acceptPendingTransfer(Long transactionId) {
		retryExecutor.executeWithRetry(() -> pendingTransferService.accept(transactionId));
	}

	@Transactional
	public void cancelPendingTransfer(Long transactionId) {
		retryExecutor.executeWithRetry(() -> pendingTransferService.cancel(transactionId));
	}
}
