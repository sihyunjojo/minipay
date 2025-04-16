package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EntityReferenceRepository {

	// Spring Boot는 EntityManager를 스프링 빈으로 자동 등록함
	// @PersistenceContext 없이도 Spring Data JPA 설정만 되어 있으면 주입 가능
	private final EntityManager entityManager;

	public Member getMemberReference(Long memberId) {
		return entityManager.getReference(Member.class, memberId);
	}

	public MainAccount getMainAccountReference(Long mainAccountId) {
		return entityManager.getReference(MainAccount.class, mainAccountId);
	}

	// 필요 시 확장 가능
}
