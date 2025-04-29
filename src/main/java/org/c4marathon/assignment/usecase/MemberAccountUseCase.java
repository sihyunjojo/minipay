package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.domain.service.mainaccount.MainAccountService;
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
		Member member = memberService.registerMember(request);
		mainAccountService.createMainAccountForMember(member);

		return member.getId();
	}
}
