package org.c4marathon.assignment.domain.repository.transferlog;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long>, TransferLogQueryRepository {
}
