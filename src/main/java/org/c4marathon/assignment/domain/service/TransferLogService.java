package org.c4marathon.assignment.domain.service;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.c4marathon.assignment.domain.repository.TransferLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 서비스는 항상 비즈니스 도메인 로직만 다룬다
@Service
@RequiredArgsConstructor
public class TransferLogService {

	private final TransferLogRepository transferLogRepository;

	public void saveTransferLog(TransferLog transferLog) {
		transferLogRepository.save(transferLog);
	}

	public Slice<TransferLog> findAllBySendTimeAndIdAfterCursor(LocalDateTime startAt, Long startId, Pageable pageable) {
		return transferLogRepository.findAllBySendTimeAndIdAfterCursor(startAt, startId, pageable);
	}

	public Slice<TransferLog> findAllBySendTimeAfterCursor(LocalDateTime startAt, Pageable pageable) {
		return transferLogRepository.findAllBySendTimeAfterCursor(startAt, pageable);
	}

	public Page<TransferLog> findRecentLogs(Pageable pageable) {
		return transferLogRepository.findAllByOrderBySendTimeDescIdDesc(pageable);
	}
}
