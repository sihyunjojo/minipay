package org.c4marathon.assignment.infra.config.property;

import org.c4marathon.assignment.domain.model.account.enums.SavingType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "business.rule.saving-account")
@Getter
@Setter
public class SavingAccountPolicyProperties {

	@NotNull
	private Double fixedInterestRate = 0.05;

	@NotNull
	private Double flexibleInterestRate = 0.03;

	public double getInterestRate(SavingType type) {
		return switch (type) {
			case FIXED -> fixedInterestRate;
			case FLEXIBLE -> flexibleInterestRate;
			default -> throw new IllegalArgumentException(String.format("지원하지 않는 적금 형태입니다.: %s", type));
		};
	}
}
