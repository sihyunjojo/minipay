package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.account.AccountResponseDto;
import org.c4marathon.assignment.dto.account.CreateFixedSavingAccountRequestDto;
import org.c4marathon.assignment.usecase.SavingAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/saving")
public class SavingAccountController {

	private final SavingAccountUseCase savingAccountUseCase;

	@PostMapping("/fixed/{memberId}")
	public ResponseEntity<ApiResponse<AccountResponseDto>> createFixedSavingAccount(@PathVariable Long memberId, @RequestBody
	CreateFixedSavingAccountRequestDto request) {
		AccountResponseDto savingAccount = savingAccountUseCase.registerFixedSavingAccount(memberId, request);
		return ResponseEntity.status(201)
			.body(ApiResponse.res(201, "정기 적금 계좌 생성 완료", savingAccount));
	}

	@PostMapping("/flexible/{memberId}")
	public ResponseEntity<ApiResponse<AccountResponseDto>> createFlexibleSavingAccount(@PathVariable Long memberId) {
		AccountResponseDto savingAccount = savingAccountUseCase.registerFlexibleSavingAccount(memberId);
		return ResponseEntity.status(201)
			.body(ApiResponse.res(201, "자유 적금 계좌 생성 완료", savingAccount));
	}

	@PostMapping("/deposit")
	public ResponseEntity<ApiResponse<String>> deposit(@RequestParam Long savingAccountId, @RequestParam Long amount) {
		savingAccountUseCase.deposit(savingAccountId, amount);
		return ResponseEntity.status(200)
			.body(ApiResponse.res(200, "입금 완료"));
	}

}
