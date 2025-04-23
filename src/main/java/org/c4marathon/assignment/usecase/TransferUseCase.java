package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferRetryService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferService;
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

	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			transferRetryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
			return;
		}

		mainAccountService.chargeOrThrow(fromAccountId, shortfall, transferAmount);
		transferRetryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
	}
}
