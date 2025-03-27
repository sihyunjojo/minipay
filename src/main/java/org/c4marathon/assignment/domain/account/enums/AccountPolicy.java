package org.c4marathon.assignment.domain.account.enums;


import lombok.Getter;

@Getter
public enum AccountPolicy {
    MAIN_DAILY_LIMIT(3_000_000L),
    CHARGE_UNIT(10_000L);

    private final Long value;

    AccountPolicy(Long value) {
        this.value = value;
    }
}
