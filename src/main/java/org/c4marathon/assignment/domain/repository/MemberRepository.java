package org.c4marathon.assignment.domain.repository;

import org.c4marathon.assignment.domain.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
