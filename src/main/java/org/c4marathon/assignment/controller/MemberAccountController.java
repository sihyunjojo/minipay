package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.ApiResponse;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.service.MemberAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberAccountController {
    private final MemberAccountService memberAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> registerMemberWithAccount(
            @RequestBody MemberRegistrationRequestDto request) {
        Long memberId = memberAccountService.registerMemberWithAccount(request);
        return ResponseEntity.ok(ApiResponse.res(201, "회원 등록 및 메인 계좌 생성 완료", memberId));
    }


    @PostMapping("/2")
    public ResponseEntity<ApiResponse<Long>> registerMemberWithAccount2(
        @RequestBody MemberRegistrationRequestDto request) {
        Long memberId = memberAccountService.registerMemberWithAccount2(request);
        return ResponseEntity.ok(ApiResponse.res(201, "회원 등록 및 메인 계좌 생성 완료", memberId));
    }

    @PostMapping("/{memberId}/accounts/saving")
    public ResponseEntity<ApiResponse<AccountDto>> createSavingAccount(@PathVariable Long memberId) {
        AccountDto savingAccount = memberAccountService.registerSavingAccount(memberId);
        return ResponseEntity.ok(ApiResponse.res(201, "적금 계좌 생성 완료", savingAccount));
    }

    @PostMapping("/{memberId}/accounts/saving/2")
    public ResponseEntity<ApiResponse<AccountDto>> createSavingAccount2(@PathVariable Long memberId) {
        AccountDto savingAccount = memberAccountService.registerSavingAccount2(memberId);
        return ResponseEntity.ok(ApiResponse.res(201, "적금 계좌 생성 완료", savingAccount));
    }
}
