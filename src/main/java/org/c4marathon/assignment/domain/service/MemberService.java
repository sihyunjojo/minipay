package org.c4marathon.assignment.domain.service;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public Member registerMember(MemberRegistrationRequestDto request) {
		Member member = Member.builder()
			.name(request.name())
			.email(request.email())
			.password(request.password())
			.build();

		memberRepository.save(member);
		return member;
	}
}
