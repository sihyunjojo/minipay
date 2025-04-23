package org.c4marathon.assignment.dto.account;

import jakarta.validation.constraints.NotNull;

public record TransferRequestDto(@NotNull Long fromMemberId, @NotNull Long toMemberId, @NotNull Long amount) {
}
