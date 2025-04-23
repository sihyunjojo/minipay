package org.c4marathon.assignment.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequestDto(@NotNull Long fromAccountId, @NotNull Long toAccountId, @NotNull @Positive Long amount) {
}
