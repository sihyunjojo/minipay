package org.c4marathon.assignment.domain.model.transferlog;

import java.time.Clock;
import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.model.account.Account;
import org.c4marathon.assignment.domain.model.policy.ExternalAccountPolicy;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferType;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransferLogFactory {

	private final Clock clock;

	// 메인 → 메인 즉시 송금
	public TransferLog createImmediateTransferLog(Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(null, from, to, amount, TransferType.IMMEDIATE, TransferStatus.COMPLETED);
	}

	// 메인 → 메인 대기 송금
	public TransferLog createPendingTransferLog(Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(null, from, to, amount, TransferType.PENDING, TransferStatus.PENDING);
	}

	// 대기 송금 완료
	public TransferLog createCompletePendingTransferLog(Long parentId, Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(parentId, from, to, amount, TransferType.PENDING, TransferStatus.COMPLETED);
	}

	// 대기 송금 취소
	public TransferLog createCancelPendingTransferLog(Long parentId, Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(parentId, from, to, amount, TransferType.PENDING, TransferStatus.CANCELED);
	}

	// 대기 송금 만료
	public TransferLog createExpirePendingTransferLog(Long parentId, Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(parentId, from, to, amount, TransferType.PENDING, TransferStatus.EXPIRED);
	}

	// 메인 → 적금 송금
	public TransferLog createToSavingTransferLog(Account fromAccount, Account toAccount, long amount) {
		AccountSnapshot from = AccountSnapshot.from(fromAccount);
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(null, from, to, amount, TransferType.IMMEDIATE, TransferStatus.COMPLETED);
	}

	// 외부 → 메인 충전
	public TransferLog createExternalChargeLog(ExternalAccountPolicy externalAccount, Account toAccount, long amount) {
		AccountSnapshot from = externalAccount.snapshot();
		AccountSnapshot to = AccountSnapshot.from(toAccount);
		return buildTransferLog(null, from, to, amount, TransferType.CHARGE, TransferStatus.COMPLETED);
	}

	private TransferLog buildTransferLog(Long parentTransactionId, AccountSnapshot from, AccountSnapshot to, long amount,
		TransferType transactionType, TransferStatus transactionStatus) {
		LocalDateTime now = LocalDateTime.now(clock);
		
		// receiverTime 자동 결정
		LocalDateTime receiverTime = null;
		if (transactionType == TransferType.IMMEDIATE || transactionType == TransferType.CHARGE || 
			(transactionType == TransferType.PENDING && transactionStatus == TransferStatus.COMPLETED)) {
			receiverTime = now;
		}

		return TransferLog.builder()
			.parentTransactionId(parentTransactionId)
			.from(from)
			.to(to)
			.amount(amount)
			.type(transactionType)
			.status(transactionStatus)
			.sendTime(now)
			.receiverTime(receiverTime)
			.build();
	}
}
