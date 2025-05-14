package org.c4marathon.assignment.domain.model.policy;

import org.c4marathon.assignment.domain.model.account.enums.AccountType;
import org.c4marathon.assignment.domain.model.transferlog.AccountSnapshot;

import lombok.Getter;

@Getter
public enum ExternalAccountPolicy {
    COMPANY(-1L, "777777777777", AccountType.MAIN_ACCOUNT),
    TEMPORARY_CHARGING(-2L, "000000000000", AccountType.EXTERNAL_ACCOUNT);

    private final Long id;
    private final String number;
    private final AccountType type;

    ExternalAccountPolicy(Long id, String number, AccountType type) {
        this.id = id;
        this.number = number;
        this.type = type;
    }

    public AccountSnapshot snapshot() {
        return new AccountSnapshot(id, type, number);
    }
}
