package org.c4marathon.assignment.domain.repository.transferlog;

import static org.c4marathon.assignment.domain.model.transferlog.QTransferLog.transferLog;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.c4marathon.assignment.domain.model.transferlog.TransferLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransferLogQueryRepositoryImpl implements TransferLogQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<TransferLog> findAllByAccountNumberAndSendTimeAndIdAfterCursor(
		String accountNumber,
		LocalDateTime cursorTime,
		Long cursorId,
		int size
	) {
		BooleanExpression accountMatch = buildAccountMatch(accountNumber);
		DateTimeExpression<LocalDateTime> sortTime = buildSortTime(accountNumber);

		BooleanExpression cursorCondition = sortTime.gt(cursorTime)
			.or(sortTime.eq(cursorTime).and(transferLog.id.gt(cursorId)));

		return fetchSlice(accountMatch.and(cursorCondition), sortTime.asc(), size);
	}

	@Override
	public Slice<TransferLog> findAllByAccountNumberAndSendTimeAfterCursor(
		String accountNumber,
		LocalDateTime cursorTime,
		int size 	) {
		BooleanExpression accountMatch = buildAccountMatch(accountNumber);
		DateTimeExpression<LocalDateTime> sortTime = buildSortTime(accountNumber);

		BooleanExpression cursorCondition = sortTime.goe(cursorTime);

		return fetchSlice(accountMatch.and(cursorCondition), sortTime.asc(), size);
	}

	@Override
	public Page<TransferLog> findPageByAccountNumber(
		String accountNumber,
		Pageable pageable
	) {
		BooleanExpression accountMatch = buildAccountMatch(accountNumber);
		DateTimeExpression<LocalDateTime> sortTime = buildSortTime(accountNumber);

		List<TransferLog> results = queryFactory
			.selectFrom(transferLog)
			.where(accountMatch)
			.orderBy(sortTime.desc(), transferLog.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = Optional.ofNullable(
			queryFactory
				.select(transferLog.count())
				.from(transferLog)
				.where(accountMatch)
				.fetchOne()
		).orElse(0L);

		return new PageImpl<>(results, pageable, total);
	}


	private BooleanExpression buildAccountMatch(String accountNumber) {
		return transferLog.from.number.eq(accountNumber)
			.or(transferLog.to.number.eq(accountNumber));
	}

	private DateTimeExpression<LocalDateTime> buildSortTime(String accountNumber) {
		return new CaseBuilder()
			.when(transferLog.from.number.eq(accountNumber)).then(transferLog.sendTime)
			.when(transferLog.to.number.eq(accountNumber)).then(transferLog.receiverTime)
			.otherwise(transferLog.sendTime);
	}

	private Slice<TransferLog> fetchSlice(
		BooleanExpression condition,
		OrderSpecifier<?> sortOrder,
		int size
	) {
		List<TransferLog> results = queryFactory
			.selectFrom(transferLog)
			.where(condition)
			.orderBy(sortOrder, transferLog.id.asc())
			.limit(size + 1L)
			.fetch();

		boolean hasNext = results.size() > size;
		if (hasNext) {
			results.remove(results.size() - 1);
		}
return new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
	}

}
