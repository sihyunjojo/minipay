package org.c4marathon.assignment.dto.account;

import org.c4marathon.assignment.domain.model.account.Account;

public record AccountResponseDto(Long id, Long memberId, Long balance, String accountNumber) {
	public AccountResponseDto(Account account) {
		this(account.getId(), account.getMember().getId(), account.getBalance(), account.getAccountNumber());
	}
}
