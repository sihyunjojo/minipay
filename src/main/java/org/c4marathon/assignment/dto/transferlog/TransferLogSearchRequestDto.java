package org.c4marathon.assignment.dto.transferlog;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

public record TransferLogSearchRequestDto(

	@Schema(description = "조회 될 계좌 번호", example = "01-10-12345678")
	String accountNumber,

	@Schema(description = "조회 시작 기준 시간 (커서 기반)", example = "2024-04-28T00:00:00")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime cursorStartAt,

	@Schema(description = "커서 ID (sendTime 동일 시 순서를 구분)", example = "123")
	Long cursorId,

	@Schema(description = "페이지 크기", example = "10", defaultValue = "10")
	Integer size,

	@Schema(description = "페이지 번호 (0부터 시작) (커서 기반에서는 사용하지 않음)", example = "0", defaultValue = "0")
	Integer page
) {}
