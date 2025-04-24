package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.RetryService;
import org.c4marathon.assignment.domain.validator.MainAccountValidator;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferUseCase {

	private final AccountPolicyProperties accountPolicyProperties;
	private final MainAccountValidator mainAccountValidator;
	private final MainAccountService mainAccountService;
	private final RetryService retryService;

	// todo: 이체 실패 시, 충전 보상 로직 만들기
	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		mainAccountValidator.validateTransfer(fromAccountId, toAccountId, transferAmount);

		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall <= 0) {
			retryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
			return;
		}

		long chargeAmount = accountPolicyProperties.getRoundedCharge(shortfall);
		mainAccountService.chargeOrThrow(fromAccountId, chargeAmount, transferAmount);

		retryService.transferWithRetry(fromAccountId, toAccountId, transferAmount);
	}
}
