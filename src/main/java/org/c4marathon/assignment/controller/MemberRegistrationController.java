package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.response.ApiResponse;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.usecase.MemberAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberRegistrationController {

	private final MemberAccountUseCase memberAccountUseCase;

	@PostMapping
	public ResponseEntity<ApiResponse<Long>> registerMemberWithAccount(
		@RequestBody MemberRegistrationRequestDto request) {
		Long memberId = memberAccountUseCase.registerMemberWithAccount(request);
		return ResponseEntity.ok(ApiResponse.res(201, "회원 등록 및 메인 계좌 생성 완료", memberId));
	}

}
