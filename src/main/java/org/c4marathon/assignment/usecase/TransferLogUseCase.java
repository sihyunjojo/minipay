package org.c4marathon.assignment.usecase;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.c4marathon.assignment.domain.service.TransferLogService;
import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.dto.transferlog.TransferLogCursorPageResponse;
import org.c4marathon.assignment.dto.transferlog.TransferLogSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// DTO 조립은 Application 계층(UseCase)에서 담당
// UseCase는 도메인 흐름에 집중
// 도메인 규칙/정책(비즈니스 정책) 적용 + 흐름 제어
@Service
@RequiredArgsConstructor
public class TransferLogUseCase {

	private final TransferLogService transferLogService;

	public TransferLogCursorPageResponse findAllBySendTimeAfterCursor(TransferLogSearchRequest request) {
		LocalDateTime startAt = Optional.ofNullable(request.startAt())
			.orElse(LocalDateTime.now().minusDays(10));
		Long id = request.id();
		int page = Optional.ofNullable(request.page()).orElse(0);
		int size = Optional.ofNullable(request.size()).orElse(10);

		Pageable pageable = createPageable(page, size, Sort.by("sendTime").ascending().and(Sort.by("id").ascending()));

		Slice<TransferLog> slice = (id == null)
			? transferLogService.findAllBySendTimeAfterCursor(startAt, pageable)
			: transferLogService.findAllBySendTimeAndIdAfterCursor(startAt, id, pageable);

		return buildCursorPageResponse(slice.getContent(), slice.hasNext());
	}

	public TransferLogCursorPageResponse findAllByOffsetOrDefaultPaging(TransferLogSearchRequest request) {
		int page = Optional.ofNullable(request.page()).orElse(0);
		int size = Optional.ofNullable(request.size()).orElse(10);

		Pageable pageable = createPageable(page, size, Sort.by("sendTime").descending().and(Sort.by("id").descending()));

		Page<TransferLog> pageResult = transferLogService.findRecentLogs(pageable);

		return buildCursorPageResponse(pageResult.getContent(), pageResult.hasNext());
	}

	// 페이지 및 정렬 생성 헬퍼
	private Pageable createPageable(int page, int size, Sort sort) {
		return PageRequest.of(page, size, sort);
	}

	// 커서 기반 응답 조립 헬퍼
	private TransferLogCursorPageResponse buildCursorPageResponse(List<TransferLog> logs, boolean hasNext) {
		if (hasNext && !logs.isEmpty()) {
			TransferLog last = logs.get(logs.size() - 1);
			return TransferLogCursorPageResponse.builder()
				.data(logs)
				.hasNext(true)
				.nextCursorTime(last.getSendTime())
				.nextCursorId(last.getId())
				.build();
		}
		return TransferLogCursorPageResponse.builder()
			.data(logs)
			.hasNext(false)
			.nextCursorTime(null)
			.nextCursorId(null)
			.build();
	}

}
