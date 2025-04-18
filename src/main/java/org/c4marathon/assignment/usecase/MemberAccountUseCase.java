package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.domain.service.MainAccountService;
import org.c4marathon.assignment.domain.service.MemberService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAccountUseCase {

    private final MemberService memberService;
    private final MainAccountService mainAccountService;


    @Transactional
    public Long registerMemberWithAccount(MemberRegistrationRequestDto request) {
        Long memberId = memberService.registerMember(request);
        mainAccountService.createMainAccountForMember(memberId);

        return memberId;
    }
}
