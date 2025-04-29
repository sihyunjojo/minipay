package org.c4marathon.assignment.domain.repository.transfertransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.account.QMainAccount;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;
import org.c4marathon.assignment.domain.model.member.QMember;
import org.c4marathon.assignment.domain.model.transfer.QTransferTransaction;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransferTransactionQueryRepositoryImpl implements TransferTransactionQueryRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * 상태가 PENDING이고 expiredAt이 remindTime보다 과거인 트랜잭션들을
	 * 각 회원(Member) 기준으로 그룹화하여 반환합니다.
	 *
	 * @param remindTime 기준 시간 (ex. now - 24h)
	 * @return 회원별 PENDING 만료 대상 트랜잭션 맵
	 */
	@Override
	public Map<Member, List<TransferTransaction>> findRemindTargetGroupedByMember(LocalDateTime remindTime) {
		QTransferTransaction tx = QTransferTransaction.transferTransaction;
		QMainAccount main = QMainAccount.mainAccount;
		QMember member = QMember.member;

		List<Tuple> results = queryFactory
			.select(member, tx)
			.from(tx)
			.join(tx.fromMainAccount, main).fetchJoin()
			.join(main.member, member).fetchJoin()
			.where(
				tx.status.eq(TransferStatus.PENDING),
				tx.createdAt.loe(remindTime)
			)
			.fetch();

		return results.stream()
			.collect(Collectors.groupingBy(
				tuple -> Objects.requireNonNull(tuple.get(member)),
				Collectors.mapping(tuple -> tuple.get(tx), Collectors.toList())
			));
	}
}
