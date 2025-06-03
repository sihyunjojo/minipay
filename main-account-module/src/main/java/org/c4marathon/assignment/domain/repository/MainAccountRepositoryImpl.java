package org.c4marathon.assignment.domain.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.model.MainAccount;
import org.c4marathon.assignment.infra.persistence.jpa.JpaMainAccountRepository;
import org.c4marathon.assignment.infra.persistence.query.MainAccountQueryRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MainAccountRepositoryImpl implements MainAccountRepository {

	private final JpaMainAccountRepository jpa;
	private final MainAccountQueryRepository query;

	@Override
	public Optional<MainAccount> findById(Long id) {
		return jpa.findById(id); //----MODIFIED PART 1 START----
	}

	@Override
	public Optional<MainAccount> findByIdWithSentTransactions(Long id) {
		return jpa.findByIdWithSentTransactions(id);
	}

	@Override
	public Optional<MainAccount> findByMemberId(Long memberId) {
		return jpa.findByMemberId(memberId);
	}

	@Override
	public Optional<MainAccount> findByAccountNumber(String accountNumber) {
		return jpa.findByAccountNumber(accountNumber);
	}

	@Override
	public boolean existsByAccountNumber(String accountNumber) {
		return jpa.existsByAccountNumber(accountNumber);
	}

	@Override
	public void resetAllDailyChargeAmount() {
		jpa.resetAllDailyChargeAmount();
	}

	@Override
	public int depositByOptimistic(Long id, Long amount, Long version) {
		return jpa.depositByOptimistic(id, amount, version);
	}

	@Override
	public int withdrawByOptimistic(Long id, Long amount, Long version) {
		return jpa.withdrawByOptimistic(id, amount, version);
	}

	@Override
	public Long findMainAccountAmountById(Long id) {
		return jpa.findMainAccountAmountById(id);
	}

	@Override
	public boolean tryFastCharge(Long id, Long amount, Long minRequiredBalance, Long dailyLimit) {
		return query.tryFastCharge(id, amount, minRequiredBalance, dailyLimit);
	}
}
