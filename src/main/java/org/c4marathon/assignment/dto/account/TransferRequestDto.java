package org.c4marathon.assignment.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequestDto(@NotNull Long fromMemberId, @NotNull Long toMemberId, @NotNull @Positive Long amount) {
}
