package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.service.MemberAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member-accounts")
@RequiredArgsConstructor
public class MemberAccountController {
    private final MemberAccountService memberAccountService;

    @PostMapping("/register")
    public ResponseEntity<Long> registerMemberWithAccount(@RequestBody MemberRegistrationRequestDto request) {
        Long memberId = memberAccountService.registerMemberWithAccount(request);
        return ResponseEntity.ok(memberId);
    }
}
