package org.c4marathon.assignment.domain.model.transferlog;

import java.time.Clock;
import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.model.transfer.enums.TransferType;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;
import org.c4marathon.assignment.domain.model.account.enums.AccountType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransferLogFactory {

	private final Clock clock;

	// 메인 → 메인 즉시 송금
	public TransferLog createImmediateTransferLog(Long senderId, Long receiverId, long amount) {
		return buildTransferLog(null, senderId, receiverId, amount, TransferType.IMMEDIATE, TransferStatus.COMPLETED,
			AccountType.MAIN_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	// 메인 → 메인 대기 송금
	public TransferLog createPendingTransferLog(Long senderId, Long receiverId, long amount) {
		return buildTransferLog(null, senderId, receiverId, amount, TransferType.PENDING, TransferStatus.PENDING,
			AccountType.MAIN_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	// 대기 송금 완료
	public TransferLog createCompletePendingTransferLog(Long parentId, Long senderId, Long receiverId, long amount) {
		return buildTransferLog(parentId, senderId, receiverId, amount, TransferType.PENDING, TransferStatus.COMPLETED,
			AccountType.MAIN_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	// 대기 송금 취소
	public TransferLog createCancelPendingTransferLog(Long parentId, Long senderId, Long receiverId, long amount) {
		return buildTransferLog(parentId, senderId, receiverId, amount, TransferType.PENDING, TransferStatus.CANCELED,
			AccountType.MAIN_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	// 대기 송금 만료
	public TransferLog createExpirePendingTransferLog(Long parentId, Long senderId, Long receiverId, long amount) {
		return buildTransferLog(parentId, senderId, receiverId, amount, TransferType.PENDING, TransferStatus.EXPIRED,
			AccountType.MAIN_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	// 메인 → 적금 송금
	public TransferLog createToSavingTransferLog(Long senderId, Long receiverId, long amount) {
		return buildTransferLog(null, senderId, receiverId, amount, TransferType.IMMEDIATE, TransferStatus.COMPLETED,
			AccountType.MAIN_ACCOUNT, AccountType.SAVING_ACCOUNT);
	}

	// 외부 → 메인 충전
	public TransferLog createExternalChargeLog(Long externalAccountId, Long mainAccountId, long amount) {
		return buildTransferLog(null, externalAccountId, mainAccountId, amount, TransferType.CHARGE, TransferStatus.COMPLETED,
			AccountType.EXTERNAL_ACCOUNT, AccountType.MAIN_ACCOUNT);
	}

	private TransferLog buildTransferLog(Long parentTransactionId, Long senderId, Long receiverId, long amount,
		TransferType transactionType, TransferStatus transactionStatus,
		AccountType senderType, AccountType receiverType) {
		LocalDateTime now = LocalDateTime.now(clock);
		
		// receiverTime 자동 결정
		LocalDateTime receiverTime = null;
		if (transactionType == TransferType.IMMEDIATE || transactionType == TransferType.CHARGE || 
			(transactionType == TransferType.PENDING && transactionStatus == TransferStatus.COMPLETED)) {
			receiverTime = now;
		}

		return TransferLog.builder()
			.parentTransactionId(parentTransactionId)
			.senderAccountId(senderId)
			.receiverAccountId(receiverId)
			.amount(amount)
			.type(transactionType)
			.status(transactionStatus)
			.senderAccountType(senderType)
			.receiverAccountType(receiverType)
			.sendTime(now)
			.receiverTime(receiverTime)
			.build();
	}
}
