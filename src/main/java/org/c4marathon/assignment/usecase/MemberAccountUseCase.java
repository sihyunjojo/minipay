package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.domain.service.MainAccountService;
import org.c4marathon.assignment.domain.service.MemberService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
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

    @Transactional
    public Long registerMemberWithAccount2(MemberRegistrationRequestDto request) {
        Member member = memberService.registerMember2(request);
        mainAccountService.createMainAccountForMember2(member);

        return member.getId();
    }

}
