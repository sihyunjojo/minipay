package org.c4marathon.assignment.domain.repository;

import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
}
