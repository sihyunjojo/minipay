package org.c4marathon.assignment.controller;

import java.util.List;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.account.SettlementRequestDto;
import org.c4marathon.assignment.dto.account.SettlementResponseDto;
import org.c4marathon.assignment.usecase.SettlementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

	private final SettlementUseCase settlementUseCase;

	@PostMapping
	public ResponseEntity<ApiResponse<List<SettlementResponseDto>>> settle(
		@RequestBody @Valid SettlementRequestDto request
	) {
		try {
			List<SettlementResponseDto> result = settlementUseCase.settle(request);
			return ResponseEntity.ok(ApiResponse.res(200, "정산 성공", result));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.res(400, e.getMessage()));
		}
	}
}
