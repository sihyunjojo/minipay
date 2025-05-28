package org.c4marathon.assignment.dto.settlement;

import jakarta.validation.constraints.NotNull;

public record SettlementResponseDto(@NotNull Long participantMemberId, @NotNull Long amount) {

}
