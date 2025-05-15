package org.c4marathon.assignment.usecase;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.c4marathon.assignment.domain.model.policy.ExternalAccountPolicy;
import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.c4marathon.assignment.domain.model.transferlog.TransferLogFactory;
import org.c4marathon.assignment.domain.service.SavingAccountService;
import org.c4marathon.assignment.domain.service.TransferLogService;
import org.c4marathon.assignment.dto.transfer.TransferRequestDto;
import org.c4marathon.assignment.infra.config.property.AccountPolicyProperties;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
import org.c4marathon.assignment.domain.service.mainaccount.TransferService;
import org.c4marathon.assignment.dto.transfer.AccountNumberTransferRequestDto;
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
	private final SavingAccountService savingAccountService;
	private final TransferService transferService;
	private final TransferLogService transferLogService;
	private final TransferLogFactory transferLogFactory;
	private final RetryExecutor retryExecutor;
	private final AccountPolicyProperties accountPolicyProperties;

	// todo: 이체 실패 시, 충전 보상 로직 만들기
	// todo: 마지막에 오류 터질시, 로그는 롤백이 되는데 충전과 이체가 롤백이 안됨.
	@Transactional
	public void transfer(TransferRequestDto request) {
		// 필요한 계정 식별자 저장
		final Long fromAccountId = request.fromAccountId();
		final Long toAccountId = request.toAccountId();
		final Long amount = request.amount();

		// 잔액 부족 시 충전 처리
		handleShortfall(fromAccountId, amount);

		// 송금 실행 - MainAccount to MainAccount
		executeMainToMainTransfer(
			() -> mainAccountService.getRefreshedAccount(fromAccountId),
			() -> mainAccountService.getRefreshedAccount(toAccountId),
			amount,
			(from, to) -> transferLogFactory.createImmediateTransferLog(from, to, amount)
		);
	}

	@Transactional
	public void transferByAccountNumber(AccountNumberTransferRequestDto requestDto) {
		// 계좌번호로 계좌 조회 및 검증
		MainAccount fromAccount = mainAccountService.findByAccountNumberOrThrow(requestDto.fromAccountNumber());
		MainAccount toAccount = mainAccountService.findByAccountNumberOrThrow(requestDto.toAccountNumber());

		// 기존 ID 기반 송금 로직 재사용
		transfer(TransferRequestDto.builder()
			.fromAccountId(fromAccount.getId())
			.toAccountId(toAccount.getId())
			.amount(requestDto.amount())
			.build());
	}

	@Transactional
	public void transferFromMainToSavingByAccountNumber(AccountNumberTransferRequestDto requestDto) {
		// 계좌번호로 계좌 조회 및 검증
		MainAccount fromAccount = mainAccountService.findByAccountNumberOrThrow(requestDto.fromAccountNumber());
		SavingAccount toAccount = savingAccountService.findByAccountNumberOrThrow(requestDto.toAccountNumber());
		final Long amount = requestDto.amount();

		// 잔액 부족 확인 및 필요시 충전
		handleShortfall(fromAccount.getId(), amount);

		// 송금 실행 - MainAccount to SavingAccount
		executeMainToSavingTransfer(
			() -> mainAccountService.getRefreshedAccount(fromAccount.getId()),
			() -> savingAccountService.getRefreshedAccount(toAccount.getId()),
			amount,
			(from, to) -> transferLogFactory.createImmediateTransferLog(from, to, amount)
		);
	}

	/**
	 * 잔액 부족 시 충전을 처리하는 메서드
	 */
	private void handleShortfall(Long accountId, Long amount) {
		Long shortfall = mainAccountService.calculateShortfall(accountId, amount);

		if (shortfall > 0) {
			long chargeAmount = accountPolicyProperties.getMain().getRoundedCharge(shortfall);
			mainAccountService.chargeOrThrow(accountId, chargeAmount, amount);

			MainAccount refreshedAccount = mainAccountService.getRefreshedAccount(accountId);
			TransferLog transferLog = transferLogFactory.createExternalChargeLog(
				ExternalAccountPolicy.TEMPORARY_CHARGING, refreshedAccount, chargeAmount);
			transferLogService.saveTransferLog(transferLog);
		}
	}

	/**
	 * 일반 계좌 간(MainAccount -> MainAccount) 송금 실행 및 로그 생성을 처리하는 메서드
	 */
	private void executeMainToMainTransfer(
		Supplier<MainAccount> fromAccountSupplier,
		Supplier<MainAccount> toAccountSupplier,
		Long amount,
		BiFunction<MainAccount, MainAccount, TransferLog> transferLogCreator) {

		// 송금 실행
		Callable<Void> performTransfer = () -> {
			MainAccount from = fromAccountSupplier.get();
			MainAccount to = toAccountSupplier.get();
			transferService.transferInNewTransaction(from, to, amount);
			log.debug("일반 계좌 간 송금 완료");
			return null;
		};

		retryExecutor.executeWithRetry(performTransfer);

		// 송금 로그 생성
		MainAccount refreshedFromAccount = fromAccountSupplier.get();
		MainAccount refreshedToAccount = toAccountSupplier.get();
		TransferLog immediateTransferLog = transferLogCreator.apply(refreshedFromAccount, refreshedToAccount);
		transferLogService.saveTransferLog(immediateTransferLog);
	}

	/**
	 * 일반 계좌에서 적금 계좌로(MainAccount -> SavingAccount) 송금 실행 및 로그 생성을 처리하는 메서드
	 */
	private void executeMainToSavingTransfer(
		Supplier<MainAccount> fromAccountSupplier,
		Supplier<SavingAccount> toAccountSupplier,
		Long amount,
		BiFunction<MainAccount, SavingAccount, TransferLog> transferLogCreator) {

		// 송금 실행
		Callable<Void> performTransfer = () -> {
			MainAccount from = fromAccountSupplier.get();
			SavingAccount to = toAccountSupplier.get();
			transferService.transferInNewTransaction(from, to, amount);
			log.debug("일반 계좌에서 적금 계좌로 송금 완료");
			return null;
		};

		retryExecutor.executeWithRetry(performTransfer);

		// 송금 로그 생성
		MainAccount refreshedFromAccount = fromAccountSupplier.get();
		SavingAccount refreshedToAccount = toAccountSupplier.get();
		TransferLog immediateTransferLog = transferLogCreator.apply(refreshedFromAccount, refreshedToAccount);
		transferLogService.saveTransferLog(immediateTransferLog);
	}

	/**
	 * 함수형 인터페이스 추가 - Java 표준 Supplier와 유사하지만 예외 처리를 위해 별도 정의
	 */
	@FunctionalInterface
	private interface Supplier<T> {
		T get();
	}
}
