package org.c4marathon.assignment.domain.service;

import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

	// 성능보다 정합성이 우선되어야함.
	@Transactional
	public void transfer(MainAccount from, MainAccount to, Long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
		}
		if (from.equals(to)) {
		   throw new IllegalArgumentException("자신에게 송금할 수 없습니다.");
		}
		if (from.getBalance() < amount) {
			throw new IllegalStateException("충전해도 잔액이 부족합니다.");
		}

		from.withdraw(amount);
		to.deposit(amount);
	}
}
