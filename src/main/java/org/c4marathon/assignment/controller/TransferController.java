package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.account.TransferRequestDto;
import org.c4marathon.assignment.usecase.TransferUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// 클린 아키텍처: UseCase는 별도의 서비스로 분리
// 송금은 MainAccount 자체의 책임이 아님
// → 송금은 계좌의 서브기능이 아니라, 고유한 도메인 행위이기 때문에 TransferController로 분리하는 게 클린하고 확장 가능성도 좋다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class TransferController {

	private final TransferUseCase transferUseCase;

	@PostMapping("/transfer")
	public ResponseEntity<ApiResponse<String>> transfer(
		@RequestBody TransferRequestDto request
	) {
		try {
			transferUseCase.transfer(request.fromMemberId(), request.toMemberId(), request.amount());
			return ResponseEntity.ok(ApiResponse.res(200, "송금 성공"));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest()
				.body(ApiResponse.res(400, e.getMessage()));
		}
	}

}
