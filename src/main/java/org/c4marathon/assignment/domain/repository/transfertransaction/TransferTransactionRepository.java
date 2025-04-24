package org.c4marathon.assignment.domain.repository.transfertransaction;

import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long>, TransferTransactionQueryRepository {

	@Query("SELECT t FROM TransferTransaction t WHERE t.id = :toAccountId AND t.status = 'PENDING'")
	Optional<TransferTransaction> findPendingTransferTransactionById(@Param("toAccountId") Long toAccountId);

	@Query("SELECT t FROM TransferTransaction t LEFT JOIN t.toMainAccount m WHERE t.status = 'PENDING' AND t.expiredAt <= :remindTime")
	List<TransferTransaction> findRemindPendingTargetTransactionsWithMainAccount(LocalDateTime remindTime);

	@Query("SELECT t FROM TransferTransaction t LEFT JOIN t.toMainAccount m LEFT JOIN m.member WHERE t.status = 'PENDING' AND t.expiredAt <= :remindTime")
	List<TransferTransaction> findRemindPendingTargetTransactionsWithMember(LocalDateTime remindTime);
}
