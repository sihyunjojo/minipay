package org.c4marathon.assignment.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.PendingTransfer;
public interface PendingTransferRepository {
	Optional<PendingTransfer> findPendingById(Long id);
	List<PendingTransfer> findRemindTargetsWithMainAccount(LocalDateTime remindTime);
	List<PendingTransfer> findRemindTargetsWithMember(LocalDateTime remindTime);
}
