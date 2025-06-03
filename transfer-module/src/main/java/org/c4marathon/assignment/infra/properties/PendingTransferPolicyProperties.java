package org.c4marathon.assignment.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "transfer-policy")
public class PendingTransferPolicyProperties {
    private long pendingTransferExpireAfterDurationHours = 72;
    private long pendingTransferRemindDurationHours = 24;

    public Duration getPendingTransferExpireAfterDurationHours() {
        return Duration.ofHours(pendingTransferExpireAfterDurationHours);
    }

    public Duration getPendingTransferRemindDurationHours() {
        return Duration.ofHours(pendingTransferRemindDurationHours);
    }
}
