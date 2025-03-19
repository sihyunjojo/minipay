package org.c4marathon.assignment.service;

import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAccountService {
    private final MemberService memberService;
    private final AccountService accountService;

    @Transactional
    public Long registerMemberWithAccount(MemberRegistrationRequestDto request) {
        Long memberId = memberService.registerMember(request); 
        accountService.createMainAccountForMember(memberId);

        return memberId;
    }
}
