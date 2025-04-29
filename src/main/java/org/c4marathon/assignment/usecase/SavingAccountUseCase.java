package org.c4marathon.assignment.usecase;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.service.mainaccount.TransferService;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.SavingAccountService;
import org.c4marathon.assignment.dto.account.AccountResponseDto;
import org.c4marathon.assignment.dto.account.CreateFixedSavingAccountRequestDto;
import org.c4marathon.assignment.dto.account.SavingDepositRequest;
import org.c4marathon.assignment.infra.config.property.MainAccountPolicy;
import org.c4marathon.assignment.infra.retry.RetryExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountUseCase {

	private final MainAccountService mainAccountService;
	private final SavingAccountService savingAccountService;
	private final TransferService transferService;

	private final MainAccountPolicy mainAccountPolicy;
	private final RetryExecutor retryExecutor;

	@Transactional
	public AccountResponseDto registerFixedSavingAccount(Long memberId, CreateFixedSavingAccountRequestDto request) {
		return savingAccountService.createFixedSavingAccount(memberId, request);
	}

	@Transactional
	public AccountResponseDto registerFlexibleSavingAccount(Long memberId) {
		return savingAccountService.createFlexibleSavingAccount(memberId);
	}

	@Transactional
	public void deposit(Long savingAccountId, Long amount) {
		savingAccountService.deposit(savingAccountId, amount);
	}

	@Transactional
	public void processFixedSavingDeposits() {
		Map<MainAccount, List<SavingDepositRequest>> amountMapping = savingAccountService.getSubscribedDepositAmount();

		// 각 메인 계좌별로 처리
		for (Map.Entry<MainAccount, List<SavingDepositRequest>> entry : amountMapping.entrySet()) {
			MainAccount mainAccount = entry.getKey();
			List<SavingDepositRequest> amountResult = entry.getValue();

			// 해당 메인 계좌에서 모든 적금 계좌로 입금해야 할 총 이자 금액 계산
			long totalAmount = getTotalAmount(amountResult);

			// 잔액 부족분 확인
			long shortfall = mainAccountService.calculateShortfall(mainAccount.getId(), totalAmount);

			// 잔액 부족 시 충전 진행
			if (shortfall > 0) {
				long chargeAmount = mainAccountPolicy.getRoundedCharge(shortfall);
				mainAccountService.chargeOrThrow(mainAccount.getId(), chargeAmount, totalAmount);
			}

			// 각 적금 계좌별로 이자 입금 처리
			for (SavingDepositRequest interestResult : amountResult) {
				transferToSavingAccount(interestResult, mainAccount);
			}
		}
	}

	private void transferToSavingAccount(SavingDepositRequest interestResult, MainAccount mainAccount) {
		final Long fromAccountId = mainAccount.getId();
		final Long toAccountId = interestResult.savingAccount().getId();
		final long amount = interestResult.subscribedDepositAmount();

		try {
			Callable<Void> performTransfer = () -> {
				var from = mainAccountService.getRefreshedAccount(fromAccountId);
				var to = savingAccountService.getRefreshedAccount(toAccountId);
				transferService.transferInNewTransaction(from, to, amount);
				return null;
			};

			retryExecutor.executeWithRetry(performTransfer);
		} catch (Exception e) {
			// 에러 처리 로직 필요: 롤백 또는 보상 트랜잭션 등
			throw new RuntimeException("이자 입금 중 오류 발생: " + e.getMessage(), e);
		}
	}

	private static long getTotalAmount(List<SavingDepositRequest> interestResults) {
		return interestResults.stream()
			.mapToLong(SavingDepositRequest::subscribedDepositAmount)
			.sum();
	}

	@Transactional
	public void applyDailyInterest() {
		savingAccountService.applyInterest();
	}
}
