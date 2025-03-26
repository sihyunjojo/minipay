package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.dto.account.AccountDto;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAccountService {
    private final MemberService memberService;
    private final MainAccountService mainAccountService;
    private final SavingAccountService savingAccountService;


    @Transactional
    public Long registerMemberWithAccount(MemberRegistrationRequestDto request) {
        Long memberId = memberService.registerMember(request);
        mainAccountService.createMainAccountForMember(memberId);

        return memberId;
    }

    // @Transactional
    // public AccountDto registerSavingAccount(Long memberId) {
    //     memberService.validateMemberExists(memberId);
    //     MainAccount mainAccount = mainAccountService.getMainAccountByMemberId(memberId);
    //     return savingAccountService.createSavingAccountForMember2(memberId, mainAccount);
    // }

    @Transactional
    public AccountDto registerSavingAccount(Long memberId) {
        memberService.validateMemberExists(memberId);
        Long memberAccountId = mainAccountService.getMainAccountByMemberId2(memberId);
        return savingAccountService.createSavingAccountForMember(memberId, memberAccountId);
    }

}
