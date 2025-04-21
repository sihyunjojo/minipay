package org.c4marathon.assignment.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "business.rule.account")
@Getter
@Setter
public class AccountPolicyProperties {

	// 💡 'business.rule.account.main-daily-limit' 값이 yml에 없을 경우 3000000으로 fallback (기본값)
	@NotNull
	private Long mainDailyLimit = 3_000_000L;
	@NotNull
	private Long chargeUnit = 10_000L;
}
