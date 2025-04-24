package org.c4marathon.assignment.infra.config.property;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Setter
@Component
@ConfigurationProperties(prefix = "remind")
public class TransferTransactionPolicyProperties {
	private long pendingTransferExpireAfterDurationHour = Duration.ofHours(72).toHours();
	private long pendingTransferRemindDurationHour = Duration.ofHours(24).toHours();

	public Duration getPendingTransferExpireAfterDurationHour() {
		return Duration.ofHours(pendingTransferExpireAfterDurationHour);
	}

	public Duration getPendingTransferRemindDurationHour() {
		return Duration.ofHours(pendingTransferRemindDurationHour);
	}
}
