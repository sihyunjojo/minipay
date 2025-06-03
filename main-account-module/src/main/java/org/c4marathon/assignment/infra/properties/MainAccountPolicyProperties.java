package org.c4marathon.assignment.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "business.rule.account.main")
public class MainAccountPolicyProperties {

	// 💡 'business.rule.account.main-daily-limit' 값이 yml에 없을 경우 3000000으로 fallback (기본값)
	@NotNull
	private Long mainDailyLimit = 3_000_000L;

	@NotNull
	private Long chargeUnit = 10_000L;

	@NotNull
	private String accountPrefix = "01";

	public Long getRoundedCharge(Long shortfall) {
		return ((shortfall + chargeUnit - 1) / chargeUnit) * chargeUnit;
	}
}
