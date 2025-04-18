package org.c4marathon.assignment.domain.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.enums.AccountPolicy;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final MainAccountRepository mainAccountRepository;
	private final AccountPolicyService accountPolicyService;

	@Transactional
	public void createMainAccountForMember(Member member) {
		boolean accountExists = mainAccountRepository.findByMemberId(member.getId()).isPresent();
		if (accountExists) {
			throw new IllegalStateException("회원이 이미 메인 계좌를 가지고 있습니다.");
		}

		MainAccount mainAccount = MainAccount.builder()
			.balance(0L)
			.build();

		member.setMainAccount(mainAccount);
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

	// 📌 2. ID 기반 최신 조회 (Post-fetch용)
	public MainAccount getById(Long id) {
		return mainAccountRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("ID가 " + id + "인 메인 계좌가 존재하지 않습니다."));
	}

	@Transactional
	public boolean conditionalFastCharge(Long accountId, Long amount, Long minRequiredBalance) {
		int updated = mainAccountRepository.conditionalFastCharge(
			accountId,
			amount,
			minRequiredBalance,
			accountPolicyService.getPolicyValue(AccountPolicy.MAIN_DAILY_LIMIT)
		);

		return updated > 0;
	}

	@Transactional
	public void resetAllDailyTransferAmount() {
		mainAccountRepository.resetAllDailyTransferAmount();
	}


}
