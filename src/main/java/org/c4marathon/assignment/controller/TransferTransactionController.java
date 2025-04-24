package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.usecase.TransferTransactionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/pending-transfer")
public class TransferTransactionController {

	private final TransferTransactionUseCase transferTransactionUsecase;

	@Operation(summary = "보류 송금 시작", description = "받는 사람이 수락해야 완료되는 송금을 생성합니다.")
	@PostMapping("")
	public ResponseEntity<ApiResponse<String>> initiatePendingTransfer(
		@Parameter(description = "송신 계좌 ID", required = true, example = "1") @RequestParam Long fromAccountId,
		@Parameter(description = "수신 계좌 ID", required = true, example = "2") @RequestParam Long toAccountId,
		@Parameter(description = "송금 금액 (단위: 원)", required = true, example = "50000") @RequestParam Long amount
	) {
		try {
			transferTransactionUsecase.initiatePendingTransfer(fromAccountId, toAccountId, amount);
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
			transferTransactionUsecase.acceptPendingTransfer(transactionId);
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
			transferTransactionUsecase.cancelPendingTransfer(transactionId);
			return ResponseEntity.ok(ApiResponse.res(200, "송금 취소 완료"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(ApiResponse.res(400, e.getMessage()));
		}
	}
}
