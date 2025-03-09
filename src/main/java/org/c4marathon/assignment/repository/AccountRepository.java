package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Long> {
}
