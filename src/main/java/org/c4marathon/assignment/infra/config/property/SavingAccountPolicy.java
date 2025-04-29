package org.c4marathon.assignment.infra.config.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.c4marathon.assignment.domain.model.account.enums.SavingType;

@Getter
@Setter
public class SavingAccountPolicy {

	@NotNull
	private Double fixedInterestRate = 0.05;

	@NotNull
	private Double flexibleInterestRate = 0.03;

	@NotNull
	private String accountPrefix = "02";

	public double getInterestRate(SavingType type) {
		return switch (type) {
			case FIXED -> fixedInterestRate;
			case FLEXIBLE -> flexibleInterestRate;
			default -> throw new IllegalArgumentException("지원하지 않는 적금 유형입니다: " + type);
		};
	}
}
