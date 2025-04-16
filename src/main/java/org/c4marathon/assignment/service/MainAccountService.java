package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.domain.account.enums.AccountPolicy;
import org.c4marathon.assignment.repository.EntityReferenceRepository;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final EntityReferenceRepository entityReferenceRepository;
	private final MainAccountRepository mainAccountRepository;

	@Transactional
	public void createMainAccountForMember(Long memberId) {
		Member memberProxy = entityReferenceRepository.getMemberReference(memberId);

		MainAccount mainAccount = MainAccount.builder()
			.member(memberProxy)
			.balance(0L)
			.build();

		mainAccountRepository.save(mainAccount);
	}

	@Transactional
	public void createMainAccountForMember2(Member member) {
		MainAccount mainAccount = MainAccount.builder()
			.member(member)
			.balance(0L)
			.build();

		mainAccountRepository.save(mainAccount);
	}

	// @Transactional(readOnly = true)를 쓰면 무조건 빨라진다고 오해하지만,
	// 실제로는 JPA의 변경 감지(dirty checking)를 비활성화하는 정도
	public MainAccount getByMemberId(Long memberId) {
		return mainAccountRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 없습니다."));
	}

	public Long getMainAccountByMemberId(Long memberId) {
		return mainAccountRepository.findIdByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}


	public MainAccount getMainAccountByMemberId2(Long memberId) {
		return mainAccountRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}

	@Transactional
	public boolean conditionalFastCharge(Long accountId, Long amount, Long minRequiredBalance) {
		int updated = mainAccountRepository.conditionalFastCharge(
			accountId,
			amount,
			minRequiredBalance,
			AccountPolicy.MAIN_DAILY_LIMIT.getValue()
		);

		return updated > 0;
	}


}
