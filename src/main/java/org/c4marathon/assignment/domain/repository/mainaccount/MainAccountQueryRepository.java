package org.c4marathon.assignment.domain.repository.mainaccount;

public interface MainAccountQueryRepository {
	int conditionalFastCharge(Long id, Long amount, Long minRequiredBalance, Long limit);
}
