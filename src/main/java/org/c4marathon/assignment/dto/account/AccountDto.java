package org.c4marathon.assignment.dto.account;

import org.c4marathon.assignment.domain.account.Account;

public record AccountDto(Long id, Long memberId, Long balance) {
    public AccountDto(Account account) {
        this(account.getId(), account.getMember().getId(), account.getBalance());
    }
}
