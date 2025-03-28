package org.c4marathon.assignment.repository;

public interface MainAccountQueryRepository {
	int conditionalFastCharge(Long id, Long amount, Long minRequiredBalance, Long limit);
}
