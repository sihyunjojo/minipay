package org.c4marathon.assignment.domain.service;

import static org.c4marathon.assignment.domain.model.account.enums.AccountPolicy.*;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.config.AccountPolicyProperties;
import org.c4marathon.assignment.domain.model.account.enums.AccountPolicy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountPolicyService {

	private final AccountPolicyProperties properties;

	public Long getPolicyValue(AccountPolicy type) {
		return switch (type) {
			case MAIN_DAILY_LIMIT -> properties.getMainDailyLimit();
			case CHARGE_UNIT -> properties.getChargeUnit();
		};
	}

	public Long getRoundedCharge(Long shortfall) {
		Long unit = getPolicyValue(CHARGE_UNIT);
		return ((shortfall + unit - 1) / unit) * unit;
	}
}
