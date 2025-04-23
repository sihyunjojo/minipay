package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferUseCase {

	private final MainAccountService mainAccountService;
	private final TransferService transferService;

	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			transferService.transfer(fromAccountId, toAccountId, transferAmount);
			return;
		}

		mainAccountService.chargeOrThrow(fromAccountId, shortfall, transferAmount);

		transferService.transfer(fromAccountId, toAccountId, transferAmount);
	}
}
