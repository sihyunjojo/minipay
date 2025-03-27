package org.c4marathon.assignment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MainAccountService {

	@PersistenceContext
	private EntityManager entityManager;

	private final MainAccountRepository mainAccountRepository;

	@Transactional
	public void createMainAccountForMember(Long memberId) {
		Member memberProxy = entityManager.getReference(Member.class, memberId);

		MainAccount mainAccount = MainAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.build();

		mainAccountRepository.save(mainAccount);
	}

	public Long getMainAccountByMemberId(Long memberId) {
		return mainAccountRepository.findIdByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}


	public void validateMainAccountExists(Long memberId) {
		Member memberProxy = entityManager.getReference(Member.class, memberId);

		boolean exists = mainAccountRepository.existsByMember(memberProxy);
		if (!exists) {
			throw new IllegalStateException("메인 계좌가 존재하지 않아 적금 계좌를 생성할 수 없습니다.");
		}
	}
}
