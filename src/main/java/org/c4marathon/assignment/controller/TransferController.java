package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.account.TransferRequestDto;
import org.c4marathon.assignment.usecase.TransferUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 클린 아키텍처: UseCase는 별도의 서비스로 분리
// 송금은 MainAccount 자체의 책임이 아님
// → 송금은 계좌의 서브기능이 아니라, 고유한 도메인 행위이기 때문에 TransferController로 분리하는 게 클린하고 확장 가능성도 좋다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/transfer")
public class TransferController {

	private final TransferUseCase transferUseCase;

	@Operation(summary = "즉시 송금 요청", description = "보류 없이 바로 송금하는 API입니다.")
	@PostMapping("")
	public ResponseEntity<ApiResponse<String>> transfer(
		@Valid @RequestBody TransferRequestDto request
	) {
		try {
			transferUseCase.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
			return ResponseEntity.ok(ApiResponse.res(200, "송금 성공"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.res(400, e.getMessage()));
		}
	}

	@Operation(summary = "보류 송금 시작", description = "받는 사람이 수락해야 완료되는 송금을 생성합니다.")
	@PostMapping("/pending")
	public ResponseEntity<ApiResponse<String>> initiatePendingTransfer(
		@Parameter(description = "송신 계좌 ID", required = true, example = "1") @RequestParam Long fromAccountId,
		@Parameter(description = "수신 계좌 ID", required = true, example = "2") @RequestParam Long toAccountId,
		@Parameter(description = "송금 금액 (단위: 원)", required = true, example = "50000") @RequestParam Long amount
	) {
		try {
			transferUseCase.initiatePendingTransfer(fromAccountId, toAccountId, amount);
			return ResponseEntity.ok(ApiResponse.res(200, "Pending 송금 요청 완료"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.res(400, e.getMessage()));
		}
	}

	@Operation(summary = "보류 송금 수락", description = "수신자가 보류 중인 송금을 수락하여 입금을 완료합니다.")
	@PostMapping("/accept")
	public ResponseEntity<ApiResponse<String>> acceptPendingTransfer(
		@Parameter(description = "보류 거래 ID", required = true, example = "2") @RequestParam Long transactionId
	) {
		try {
			transferUseCase.acceptPendingTransfer(transactionId);
			return ResponseEntity.ok(ApiResponse.res(200, "송금 수령 완료"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.res(400, e.getMessage()));
		}
	}

	@Operation(summary = "보류 송금 취소", description = "송신자가 보류 중인 송금을 취소합니다.")
	@PostMapping("/cancel")
	public ResponseEntity<ApiResponse<String>> cancelPendingTransfer(
		@Parameter(description = "보류 거래 ID", required = true, example = "1") @RequestParam Long transactionId
	) {
		try {
			transferUseCase.cancelPendingTransfer(transactionId);
			return ResponseEntity.ok(ApiResponse.res(200, "송금 취소 완료"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.res(400, e.getMessage()));
		}
	}
}
