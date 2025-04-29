package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.domain.service.TransferTransactionService;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.validator.MainAccountValidator;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransferTransactionUseCase {

	private final MainAccountValidator mainAccountValidator;

	private final MainAccountService mainAccountService;
	private final TransferTransactionService transferTransactionService;

	private final AccountPolicyProperties accountPolicyProperties;
	private final RetryExecutor retryExecutor;

	@Transactional
	public void initiatePendingTransfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			retryExecutor.executeWithRetry(() -> transferTransactionService.initiate(fromAccountId, toAccountId, transferAmount));
			return;
		}

		long chargeAmount = accountPolicyProperties.getMain().getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(fromAccountId, chargeAmount, transferAmount);

		retryExecutor.executeWithRetry(() -> transferTransactionService.initiate(fromAccountId, toAccountId, transferAmount));
	}

	@Transactional
	public void acceptPendingTransfer(Long transactionId) {
		retryExecutor.executeWithRetry(() -> transferTransactionService.accept(transactionId));
	}

	@Transactional
	public void cancelPendingTransfer(Long transactionId) {
		retryExecutor.executeWithRetry(() -> transferTransactionService.cancel(transactionId));
	}
}
