package org.c4marathon.assignment.controller;

import io.swagger.v3.oas.annotations.Operation;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.transferlog.TransferLogCursorPageResponse;
import org.c4marathon.assignment.dto.transferlog.TransferLogSearchRequest;
import org.c4marathon.assignment.usecase.TransferLogUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 	Controller는 HTTP에 집중
// Controller에서는 절대 정책이 있으면 안 되고
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfer-logs")
public class TransferLogController {

	private final TransferLogUseCase transferLogUseCase;

	@Operation(summary = "송금 이력 조회", description = "startAt를 기준으로 커서 기반 송금 이력을 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<TransferLogCursorPageResponse>> getTransferLogs(
		@Valid TransferLogSearchRequest request
	) {
		TransferLogCursorPageResponse result;

		if (request.startAt() != null) {
			result = transferLogUseCase.findAllBySendTimeAfterCursor(request);
		} else {
			result = transferLogUseCase.findAllByOffsetOrDefaultPaging(request);
		}

		return ResponseEntity.ok(ApiResponse.res(200, "송금 이력 조회 성공", result));
	}
}
