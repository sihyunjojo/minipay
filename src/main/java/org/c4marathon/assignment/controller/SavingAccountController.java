package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.usecase.SavingAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/saving")
public class SavingAccountController {

	private final SavingAccountUseCase savingAccountUseCase;

	@PostMapping("/{memberId}/accounts/saving")
	public ResponseEntity<ApiResponse<AccountDto>> createSavingAccount(@PathVariable Long memberId) {
		AccountDto savingAccount = savingAccountUseCase.registerSavingAccount(memberId);
		return ResponseEntity.ok(ApiResponse.res(201, "적금 계좌 생성 완료", savingAccount));
	}
}
