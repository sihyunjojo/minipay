package org.c4marathon.assignment.domain.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.dto.member.MemberRegistrationRequestDto;
import org.c4marathon.assignment.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long registerMember(MemberRegistrationRequestDto request) {
        Member member = Member.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .build();

        memberRepository.save(member);
        return member.getId();
    }

    public void validateMemberExists(Long memberId) {
        boolean exists = memberRepository.existsById(memberId);
        if (!exists) {
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }
    }
}
