package org.c4marathon.assignment.domain.enums;

import lombok.Getter;

@Getter
public enum AccountType {
    MAIN(3_000_000L),   // 메인 계좌 출전 한도: 3,000,000원
    SAVING(null);       // 세이빙 계좌 출전 한도: 아직 미정 (null)

    private final Long withdrawalLimit; // 출전 한도

    AccountType(Long withdrawalLimit) {
        this.withdrawalLimit = withdrawalLimit;
    }
}
