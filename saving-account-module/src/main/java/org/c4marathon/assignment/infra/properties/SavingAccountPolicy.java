package org.c4marathon.assignment.infra.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.c4marathon.assignment.enums.SavingType;

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
			case SavingType.FIXED -> fixedInterestRate;
			case SavingType.FLEXIBLE -> flexibleInterestRate;
			default -> throw new IllegalArgumentException("지원하지 않는 적금 유형입니다: " + type);
		};
	}
}
