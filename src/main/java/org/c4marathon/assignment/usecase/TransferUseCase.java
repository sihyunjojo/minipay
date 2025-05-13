package org.c4marathon.assignment.usecase;

import static org.c4marathon.assignment.domain.model.enums.PolicyType.*;

import java.util.concurrent.Callable;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.c4marathon.assignment.domain.model.transferlog.TransferLogFactory;
import org.c4marathon.assignment.domain.service.TransferLogService;
import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferService;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
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
	private final TransferLogService transferLogService;
	private final TransferLogFactory transferLogFactory;

	private final RetryExecutor retryExecutor;
	private final AccountPolicyProperties accountPolicyProperties;


	// todo: 이체 실패 시, 충전 보상 로직 만들기
	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long transferAmount) {
		Long shortfall = mainAccountService.calculateShortfall(fromAccountId, transferAmount);

		if (shortfall > 0) {
			long chargeAmount = accountPolicyProperties.getMain().getRoundedCharge(shortfall);
			mainAccountService.chargeOrThrow(fromAccountId, chargeAmount, transferAmount);

			TransferLog transferLog = transferLogFactory.createExternalChargeLog(
				TEMPORARY_CHARGING_ID.getValue(),
				fromAccountId,
				chargeAmount);
			transferLogService.saveTransferLog(transferLog);
		}

		Callable<Void> performTransfer = () -> {
			var from = mainAccountService.getRefreshedAccount(fromAccountId);
			var to = mainAccountService.getRefreshedAccount(toAccountId);
			transferService.transferInNewTransaction(from, to, transferAmount);
			log.debug("송금 완료 (충전 여부: {})", shortfall > 0 ? "충전됨" : "충전 없이 처리");
			return null;
		};

		retryExecutor.executeWithRetry(performTransfer);

		TransferLog immediateTransferLog = transferLogFactory.createImmediateTransferLog(fromAccountId, toAccountId,
			transferAmount);
		transferLogService.saveTransferLog(immediateTransferLog);

	}
}
