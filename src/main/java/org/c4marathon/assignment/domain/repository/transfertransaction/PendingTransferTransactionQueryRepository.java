package org.c4marathon.assignment.domain.repository.transfertransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.transfer.PendingTransferTransaction;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface PendingTransferTransactionQueryRepository {
    Map<Member, List<PendingTransferTransaction>> findRemindTargetGroupedByMember(@Param("remindTime") LocalDateTime remindTime);
}
