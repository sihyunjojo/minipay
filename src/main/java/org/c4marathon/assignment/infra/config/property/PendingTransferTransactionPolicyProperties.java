package org.c4marathon.assignment.infra.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "remind")
public class PendingTransferTransactionPolicyProperties {
    private long pendingTransferExpireAfterDurationHours = Duration.ofHours(72).toHours();
    private long pendingTransferRemindDurationHours = Duration.ofHours(24).toHours();

    public Duration getPendingTransferExpireAfterDurationHours() {
        return Duration.ofHours(pendingTransferExpireAfterDurationHours);
    }

    public Duration getPendingTransferRemindDurationHours() {
        return Duration.ofHours(pendingTransferRemindDurationHours);
    }
}
