package org.c4marathon.assignment.domain.model.transferlog;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.c4marathon.assignment.common.model.BaseTimeEntity;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferType;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;

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

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "id", column = @Column(name = "from_account_id")),
		@AttributeOverride(name = "type", column = @Column(name = "from_account_type")),
		@AttributeOverride(name = "number", column = @Column(name = "from_account_number"))
	})
	private AccountSnapshot from;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "id", column = @Column(name = "to_account_id")),
		@AttributeOverride(name = "type", column = @Column(name = "to_account_type")),
		@AttributeOverride(name = "number", column = @Column(name = "to_account_number"))
	})
	private AccountSnapshot to;

	@Column(nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransferType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransferStatus status;

	@Column(nullable = true)
	private LocalDateTime sendTime;

	@Column(nullable = true)
	private LocalDateTime receiverTime;
}
