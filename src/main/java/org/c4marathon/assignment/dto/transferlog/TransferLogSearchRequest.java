package org.c4marathon.assignment.dto.transferlog;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

public record TransferLogSearchRequest(

	@Schema(description = "조회 시작 기준 시간 (커서 기반)", example = "2024-04-28T00:00:00")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	LocalDateTime startAt,

	@Schema(description = "커서 ID (sendTime 동일 시 순서를 구분)", example = "123")
	Long id,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
	Integer page,

	@Schema(description = "페이지 크기", example = "10", defaultValue = "10")
	Integer size

) {}
