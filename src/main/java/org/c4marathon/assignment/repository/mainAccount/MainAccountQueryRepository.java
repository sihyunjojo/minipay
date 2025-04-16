package org.c4marathon.assignment.repository.mainAccount;

public interface MainAccountQueryRepository {
	int conditionalFastCharge(Long id, Long amount, Long minRequiredBalance, Long limit);
}
