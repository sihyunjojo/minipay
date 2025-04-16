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

    // 💡 단위에 맞춰 반올림 충전 계산
    public static Long getRoundedCharge(Long shortfall) {
        Long unit = CHARGE_UNIT.getValue();
        return ((shortfall + unit - 1) / unit) * unit;
    }
}
