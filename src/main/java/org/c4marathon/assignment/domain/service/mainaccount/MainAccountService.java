package org.c4marathon.assignment.domain.service.mainaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.infra.config.property.MainAccountPolicyProperties;
import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final MainAccountPolicyProperties mainAccountPolicyProperties;
	private final MainAccountRepository mainAccountRepository;

	@Transactional
	public void createMainAccountForMember(Member member) {
		boolean accountExists = mainAccountRepository.findByMemberId(member.getId()).isPresent();
		if (accountExists) {
			throw new IllegalStateException("회원이 이미 메인 계좌를 가지고 있습니다.");
		}

		MainAccount mainAccount = MainAccount.builder().balance(0L).build();

		member.setMainAccount(mainAccount);
		mainAccountRepository.save(mainAccount);
	}

	// @Transactional(readOnly = true)를 쓰면 무조건 빨라진다고 오해하지만,
	// 실제로는 JPA의 변경 감지(dirty checking)를 비활성화하는 정도
	@Transactional(readOnly = true)
	public MainAccount findByMemberId(Long memberId) {
		return mainAccountRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public MainAccount getRefreshedAccount(Long accountId) {
		return mainAccountRepository.findByIdWithoutSecondCache(accountId)
			.orElseThrow(() -> new IllegalStateException(String.format("ID가 %s인 메인 계좌가 존재하지 않습니다.", accountId)));
	}

	@Transactional
	public void resetAllDailyChargeAmount() {
		mainAccountRepository.resetAllDailyChargeAmount();
	}

	public Long calculateShortfall(Long accountId, Long transferAmount) {
		Long currentBalance = mainAccountRepository.findMainAccountAmountById(accountId);
		long diff = transferAmount - currentBalance;

		return Math.max(diff, 0L);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void chargeOrThrow(Long accountId, Long chargeAmount, Long minRequiredBalance) {
		boolean success = mainAccountRepository.tryFastCharge(accountId, chargeAmount, minRequiredBalance,
			mainAccountPolicyProperties.getMainDailyLimit());

		if (!success) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}

}
