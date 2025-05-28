package org.c4marathon.assignment.domain.repository.transfertransaction;

import org.c4marathon.assignment.domain.model.transfer.PendingTransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PendingTransferTransactionRepository extends JpaRepository<PendingTransferTransaction, Long>, PendingTransferTransactionQueryRepository {

    @Query("SELECT t FROM PendingTransferTransaction t WHERE t.id = :toAccountId AND t.status = 'PENDING'")
    Optional<PendingTransferTransaction> findPendingPendingTransferTransactionById(@Param("toAccountId") Long toAccountId);

    @Query("SELECT t FROM PendingTransferTransaction t LEFT JOIN t.toMainAccount m WHERE t.status = 'PENDING' AND t.expiredAt <= :remindTime")
    List<PendingTransferTransaction> findRemindPendingTargetTransactionsWithMainAccount(@Param("remindTime") LocalDateTime remindTime);

    @Query("SELECT t FROM PendingTransferTransaction t LEFT JOIN t.toMainAccount m LEFT JOIN m.member WHERE t.status = 'PENDING' AND t.createdAt <= :remindTime")
    List<PendingTransferTransaction> findRemindPendingTargetTransactionsWithMember(@Param("remindTime") LocalDateTime remindTime);
}
