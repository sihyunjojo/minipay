package org.c4marathon.assignment.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.SavingAccount;

public interface SavingAccountRepository {

	List<SavingAccount> findAllFixedSavingAccountWithMemberAndMainAccount();

	Optional<SavingAccount> findByIdWithoutSecondCache(Long id);

	int depositByOptimistic(Long accountId, Long amount, Long version);

	boolean existsByAccountNumber(String accountNumber);

	Optional<SavingAccount> findByAccountNumber(String accountNumber);

	SavingAccount save(SavingAccount account);

}
