package org.c4marathon.assignment.config.property;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "remind")
public class TransferTransactionPolicyProperties {
	private long pendingTransferExpireAfterDuration = Duration.ofHours(24).toHours();
	private long pendingTransferRemindDuration = Duration.ofHours(72).toHours();

	public Duration getpendingTransferExpireAfterDuration() {
		return Duration.ofHours(pendingTransferExpireAfterDuration);
	}

	public Duration getpendingTransferRemindDuration() {
		return Duration.ofHours(pendingTransferRemindDuration);
	}
}
