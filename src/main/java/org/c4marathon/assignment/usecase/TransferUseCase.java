package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.service.MainAccountService;
import org.c4marathon.assignment.domain.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferUseCase {

	private final MainAccountService mainAccountService;
	private final TransferService transferDomainService;

	@Transactional
	public void transfer(Long fromMemberId, Long toMemberId, Long amount) {
		org.c4marathon.assignment.domain.model.account.MainAccount from = mainAccountService.getByMemberId(fromMemberId);
		org.c4marathon.assignment.domain.model.account.MainAccount to = mainAccountService.getByMemberId(toMemberId);

		Long currentBalance = from.getBalance();
		long shortfall = amount - currentBalance;

		if (shortfall <= 0) {
			transferDomainService.transfer(from, to, amount);
			return;
		}

		// 🧩 2. Assert in DB (DB 정합성 확인하며 충전 시도)
		long chargeAmount = org.c4marathon.assignment.domain.model.account.enums.AccountPolicy.getRoundedCharge(shortfall);

		if (!mainAccountService.conditionalFastCharge(from.getId(), chargeAmount, amount)) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}

		// 🧩 3. Post-fetch (DB 기준으로 동기화)
		from = mainAccountService.getById(from.getId()); // 최신 잔액 반영된 상태

		// 🧩 4. Transfer
		transferDomainService.transfer(from, to, amount);
	}
}
