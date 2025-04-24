package org.c4marathon.assignment.domain.repository.transfertransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;

public interface TransferTransactionQueryRepository {
	Map<Member, List<TransferTransaction>> findRemindTargetGroupedByMember(LocalDateTime remindTime);
}
