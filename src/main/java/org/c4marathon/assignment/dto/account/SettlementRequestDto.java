package org.c4marathon.assignment.dto.account;

import java.util.List;

import org.c4marathon.assignment.domain.model.enums.SettlementPolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SettlementRequestDto(

	@NotNull
	@Schema(description = "정산 방식", defaultValue = "EQUAL", allowableValues = {"EQUAL", "RANDOM"})
	SettlementPolicy type,

	@NotNull
	@Schema(description = "전체 정산 금액", example = "20000")
	long totalAmount,

	@NotNull
	@Schema(description = "참여자 ID 목록", example = "[1,2,3]")
	List<Long> participantMemberIdList

) {}
