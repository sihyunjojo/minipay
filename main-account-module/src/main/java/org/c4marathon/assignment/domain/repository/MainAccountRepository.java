package org.c4marathon.assignment.domain.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.model.MainAccount;

public interface MainAccountRepository {
	Optional<MainAccount> findById(Long id);

	Optional<MainAccount> findByIdWithSentTransactions(Long id);

	Optional<MainAccount> findByMemberId(Long memberId);

	Optional<MainAccount> findByAccountNumber(String accountNumber);

	boolean existsByAccountNumber(String accountNumber);

	void resetAllDailyChargeAmount();

	int depositByOptimistic(Long id, Long amount, Long version);

	int withdrawByOptimistic(Long id, Long amount, Long version);

	Long findMainAccountAmountById(Long id);

	boolean tryFastCharge(Long id, Long amount, Long minRequiredBalance, Long dailyLimit);
}
