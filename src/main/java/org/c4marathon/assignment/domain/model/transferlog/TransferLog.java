package org.c4marathon.assignment.domain.model.transferlog;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.c4marathon.assignment.common.model.BaseTimeEntity;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferType;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;
import org.c4marathon.assignment.domain.model.account.enums.AccountType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
// todo: index 추가
// @Table(name = "transfer_log", indexes = {
// 	@Index(name = "idx_sender_receiver_time", columnList = "senderAccountId, receiverAccountId, sendTime")
// })
public class TransferLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = true)
	private Long parentTransactionId;

	@Column(nullable = false)
	private Long senderAccountId;

	@Column(nullable = false)
	private Long receiverAccountId;

	@Column(nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransferType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransferStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType senderAccountType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType receiverAccountType;

	@Column(nullable = false)
	private LocalDateTime sendTime;

	@Column(nullable = true)
	private LocalDateTime receiverTime;
}
