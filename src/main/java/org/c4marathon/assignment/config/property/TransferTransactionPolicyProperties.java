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
	private long pendingExpiredThresholdHours = Duration.ofHours(24).toHours();
	private long transactionExpiredAfterHours = Duration.ofHours(72).toHours();

	public Duration getPendingExpiredThresholdDuration() {
		return Duration.ofHours(pendingExpiredThresholdHours);
	}

	public Duration getTransactionExpiredAfterDuration() {
		return Duration.ofHours(transactionExpiredAfterHours);
	}
}
