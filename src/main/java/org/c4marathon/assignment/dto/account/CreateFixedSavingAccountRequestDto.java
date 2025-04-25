package org.c4marathon.assignment.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정기 적금 계좌 생성 요청 DTO")
public record CreateFixedSavingAccountRequestDto(
	@Schema(description = "가입 금액 (원)", example = "100000")
	Long subscribedDepositAmount
) {}
