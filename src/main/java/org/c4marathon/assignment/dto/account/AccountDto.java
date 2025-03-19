package org.c4marathon.assignment.dto.account;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.enums.AccountType;

public record AccountDto(Long id, Long memberId, Long balance, AccountType accountType) {
    public AccountDto(Account account) {
        this(account.getId(), account.getMember().getId(), account.getBalance(), account.getAccountType());
    }
}
