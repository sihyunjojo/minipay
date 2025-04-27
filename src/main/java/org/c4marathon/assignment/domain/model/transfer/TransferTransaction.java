package org.c4marathon.assignment.domain.model.transfer;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.c4marathon.assignment.common.model.BaseTimeEntity;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.enums.TransferStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransferTransaction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Builder.Default
	private Long amount = 0L;

	@Enumerated(EnumType.STRING)
	private TransferStatus status;

	private LocalDateTime expiredAt;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_main_account_id", nullable = false)
	private MainAccount fromMainAccount;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_main_account_id", nullable = false)
	private MainAccount toMainAccount;

	public static TransferTransaction createPending(MainAccount fromMainAccount, MainAccount toMainAccount, Long amount, Duration expireAfter) {
		LocalDateTime expiredAt = LocalDateTime.now().plus(expireAfter);

		TransferTransaction transferTransaction = new TransferTransaction(
			null,
			amount,
			TransferStatus.PENDING,
			expiredAt,
			fromMainAccount,
			toMainAccount
		);
		fromMainAccount.addSentTransaction(transferTransaction);
		toMainAccount.addReceivedTransaction(transferTransaction);

		return transferTransaction;
	}

	public void markAsCompleted() {
		this.status = TransferStatus.COMPLETED;
	}

	public void markAsCanceled() {
		this.status = TransferStatus.CANCELED;
	}

	public void markAsExpired() {
		this.status = TransferStatus.EXPIRED;
	}
}
