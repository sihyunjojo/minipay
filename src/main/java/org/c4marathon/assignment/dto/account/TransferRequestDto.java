package org.c4marathon.assignment.dto.account;

public record TransferRequestDto(
	Long fromMemberId,
	Long toMemberId,
	Long amount
) {}
