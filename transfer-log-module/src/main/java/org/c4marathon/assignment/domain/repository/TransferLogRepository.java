package org.c4marathon.assignment.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.TransferLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TransferLogRepository {

	Optional<TransferLog> findById(Long id); // 기본 단건 조회

	TransferLog save(TransferLog transferLog); // 저장

	Slice<TransferLog> findAllByAccountNumberAndSendTimeAndIdAfterCursor(
		String accountNumber, LocalDateTime cursorTime, Long cursorId, int size); // 커서 기반 조회 (id 포함)

	Slice<TransferLog> findAllByAccountNumberAndSendTimeAfterCursor(
		String accountNumber, LocalDateTime cursorTime, int size); // 커서 기반 조회 (id 없이 시간 기준)

	Page<TransferLog> findPageByAccountNumber(String accountNumber, Pageable pageable); // 페이지 기반 조회
}
