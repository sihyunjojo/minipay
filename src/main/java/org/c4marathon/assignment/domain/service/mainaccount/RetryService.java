package org.c4marathon.assignment.domain.service.mainaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.service.TransferTransactionService;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {

	private final RetryExecutor retryExecutor;
	private final MainAccountService mainAccountService;
	private final TransferService transferService;
	private final TransferTransactionService transferTransactionService;


	/**
	 * 이체를 재시도 로직과 함께 수행
	 * 성능보다 정합성이 우선되어야함.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void transferWithRetry(Long fromAccountId, Long toAccountId, Long transferAmount) {
		MainAccount[] accounts = new MainAccount[2];

		retryExecutor.executeWithRetry(() -> {
			// 재시도할 때마다 새로운 트랜잭션 컨텍스트에서 최신 엔티티 조회
			accounts[0] = mainAccountService.getRefreshedAccount(fromAccountId);
			accounts[1] = mainAccountService.getRefreshedAccount(toAccountId);

			// 새 트랜잭션에서 이체 수행
			return transferService.transferInNewTransaction(accounts[0], accounts[1], transferAmount);
		});
	}
}
