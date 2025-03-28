package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.account.QMainAccount;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MainAccountQueryRepositoryImpl implements MainAccountQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QMainAccount mainAccount = QMainAccount.mainAccount;

	@Override
	@Transactional
	public int conditionalFastCharge(Long id, Long amount, Long minRequiredBalance, Long dailyLimit) {
		return (int) queryFactory
			.update(mainAccount)
			.set(mainAccount.balance, mainAccount.balance.add(amount))
			.set(mainAccount.dailyChargeAmount, mainAccount.dailyChargeAmount.add(amount))
			.where(
				mainAccount.id.eq(id),
				mainAccount.balance.add(amount).goe(minRequiredBalance),
				mainAccount.dailyChargeAmount.add(amount).loe(dailyLimit)
			)
			.execute();
	}
}
