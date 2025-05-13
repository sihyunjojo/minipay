package org.c4marathon.assignment.domain.repository;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import lombok.NonNull;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {

	// "커서(cursor)" = 마지막 조회한 데이터의 고유 기준값
	// 조회 결과 마지막 데이터의 커서를 기억해서" → "그 이후를 기준으로 조회를 이어간다
	@Query("""
		    SELECT t FROM TransferLog t
		    WHERE (t.sendTime > :sendTime)
		       OR (t.sendTime = :sendTime AND t.id > :id)
		    ORDER BY t.sendTime ASC, t.id ASC
		""")
	Slice<TransferLog> findAllBySendTimeAndIdAfterCursor(@Param("sendTime") @NonNull LocalDateTime sendTime,
		@Param("id") @NonNull Long id, Pageable pageable);

	@Query("""
		    SELECT t FROM TransferLog t
		    WHERE t.sendTime > :sendTime
		       OR t.sendTime = :sendTime
		    ORDER BY t.sendTime ASC, t.id ASC
		""")
	Slice<TransferLog> findAllBySendTimeAfterCursor(@Param("sendTime") @NonNull LocalDateTime sendTime,
		Pageable pageable);

	Page<TransferLog> findAllByOrderBySendTimeDescIdDesc(Pageable pageable);
}
