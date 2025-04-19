package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferUseCase {

	private final MainAccountService mainAccountService;

	@Transactional
	public void transfer(Long fromMemberId, Long toMemberId, Long transferAmount) {
		Long fromAccountId = mainAccountService.getMainAccountByMemberId(fromMemberId);
		Long toAccountId = mainAccountService.getMainAccountByMemberId(toMemberId);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			mainAccountService.transfer(fromAccountId, toAccountId, transferAmount);
			return;
		}

		mainAccountService.chargeOrThrow(fromAccountId, shortfall, transferAmount);

		mainAccountService.transfer(fromAccountId, toAccountId, transferAmount);
	}
}
