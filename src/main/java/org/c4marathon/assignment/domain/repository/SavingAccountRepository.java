package org.c4marathon.assignment.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.account.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

	@Query("SELECT sa FROM SavingAccount sa " +
		"JOIN FETCH sa.member m " +
		"JOIN FETCH m.mainAccount " +
		"WHERE sa.savingType = 'FIXED'")
	List<SavingAccount> findAllFixedSavingAccountWithMemberAndMainAccount();

	@Query("SELECT s FROM SavingAccount s WHERE s.id = :id")
	@QueryHints({
		@QueryHint(name = "org.hibernate.cacheable", value = "false"),
		@QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS"),
		@QueryHint(name = "jakarta.persistence.cache.storeMode", value = "REFRESH")
	})
	Optional<SavingAccount> findByIdWithoutSecondCache(@Param("id") Long id);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE SavingAccount a SET a.balance = a.balance + :amount, a.version = a.version + 1 "
		+ "WHERE a.id = :accountId AND a.version = :version")
	int depositByOptimistic(@Param("accountId") Long accountId, @Param("amount") Long amount,
		@Param("version") Long version);
}
