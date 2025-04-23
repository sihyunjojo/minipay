package org.c4marathon.assignment.domain.model.transfer;

import jakarta.persistence.*;
import lombok.*;

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

	@OneToOne(fetch = FetchType.LAZY)
	private MainAccount fromMainAccount;
	@OneToOne(fetch = FetchType.LAZY)
	private MainAccount toMainAccount;

	private Long amount;

	@Enumerated(EnumType.STRING)
	private TransferStatus status;

	private LocalDateTime completedAt;
	private LocalDateTime expiredAt;

	public boolean isExpired(LocalDateTime now) {
		return status == TransferStatus.PENDING && expiredAt.isBefore(now);
	}

	public boolean canBeCanceled() {
		return status == TransferStatus.PENDING;
	}

	public void markAsCompleted() {
		this.status = TransferStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}

	public void markAsCanceled() {
		this.status = TransferStatus.CANCELED;
	}

	public void markAsExpired() {
		this.status = TransferStatus.EXPIRED;
	}
}
