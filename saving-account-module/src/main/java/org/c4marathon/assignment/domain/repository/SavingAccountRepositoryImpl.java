package org.c4marathon.assignment.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.SavingAccount;
import org.c4marathon.assignment.infra.persistence.jpa.JpaSavingAccountRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SavingAccountRepositoryImpl implements SavingAccountRepository {
	private final JpaSavingAccountRepository jpa;

	@Override
	public List<SavingAccount> findAllFixedSavingAccountWithMemberAndMainAccount() {
		return jpa.findAllFixedSavingAccountWithMemberAndMainAccount();
	}

	@Override
	public Optional<SavingAccount> findByIdWithoutSecondCache(Long id) {
		return jpa.findByIdWithoutSecondCache(id);
	}

	@Override
	public int depositByOptimistic(Long accountId, Long amount, Long version) {
		return jpa.depositByOptimistic(accountId, amount, version);
	}

	@Override
	public boolean existsByAccountNumber(String accountNumber) {
		return jpa.existsByAccountNumber(accountNumber);
	}

	@Override
	public Optional<SavingAccount> findByAccountNumber(String accountNumber) {
		return jpa.findByAccountNumber(accountNumber);
	}

	@Override
	public SavingAccount save(SavingAccount account) {
		return jpa.save(account);
	}
}
