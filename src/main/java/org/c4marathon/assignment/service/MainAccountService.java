package org.c4marathon.assignment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.domain.account.enums.AccountPolicy;
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

	@Transactional
	public void charge(MainAccount account, Long amount) {
		if (account.getDailyChargeAmount() + amount > AccountPolicy.MAIN_DAILY_LIMIT.getValue()) {
			throw new IllegalStateException("일일 충전 한도를 초과했습니다.");
		}

		account.charge(amount);
	}

	// DB에 직접 UPDATE만 하고, 영속성 컨텍스트에는 반영되지 않음
	// @Transactional
	// public void fastCharge(MainAccount account, Long amount) {
	// 	if (account.getDailyChargeAmount() + amount > AccountPolicy.MAIN_DAILY_LIMIT.getValue()) {
	// 		throw new IllegalStateException("일일 충전 한도를 초과했습니다.");
	// 	}
	//
	// 	// 내가 들고 있는 from 객체는 여전히 이전의 상태
	// 	mainAccountRepository.fastCharge(account.getId(), amount);
	// }

	@Transactional
	public void conditionalFastCharge(Long accountId, Long amount, Long minRequiredBalance) {
		int updated = mainAccountRepository.conditionalFastCharge(
			accountId,
			amount,
			minRequiredBalance,
			AccountPolicy.MAIN_DAILY_LIMIT.getValue()
		);

		if (updated == 0) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}


}
