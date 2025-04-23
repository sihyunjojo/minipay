package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.AccountPolicyService;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferRetryService;
import org.c4marathon.assignment.domain.validator.MainAccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferUseCase {

	private final MainAccountValidator mainAccountValidator;
	private final MainAccountService mainAccountService;
	private final TransferRetryService transferRetryService;
	private final AccountPolicyService accountPolicyService;

	// todo: 이체 실패 시, 충전 보상 로직 만들기
	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			transferRetryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
			return;
		}

		long chargeAmount = accountPolicyService.getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(fromAccountId, chargeAmount, transferAmount);

		transferRetryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
	}

	@Transactional
	public void initiatePendingTransfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			transferRetryService.initiatePendingTransferWithRetry(fromAccountId, toAccountId, transferAmount);
			return;
		}

		long chargeAmount = accountPolicyService.getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(fromAccountId, chargeAmount, transferAmount);

		transferRetryService.initiatePendingTransferWithRetry(fromAccountId, toAccountId, transferAmount);
	}

	@Transactional
	public void acceptPendingTransfer(Long transactionId) {
		transferRetryService.acceptPendingTransferWthRetry(transactionId);
	}

	@Transactional
	public void cancelPendingTransfer(Long transactionId) {
		transferRetryService.cancelPendingTransferWithRetry(transactionId);
	}
}
