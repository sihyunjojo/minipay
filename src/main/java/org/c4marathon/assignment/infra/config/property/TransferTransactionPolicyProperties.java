package org.c4marathon.assignment.infra.config.property;

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

	public Duration getPendingTransferExpireAfterDuration() {
		return Duration.ofHours(pendingTransferExpireAfterDuration);
	}

	public Duration getPendingTransferRemindDuration() {
		return Duration.ofHours(pendingTransferRemindDuration);
	}
}
