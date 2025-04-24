package org.c4marathon.assignment.domain.repository.transfertransaction;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.QMember;
import org.c4marathon.assignment.domain.model.account.QMainAccount;
import org.c4marathon.assignment.domain.model.enums.TransferStatus;
import org.c4marathon.assignment.domain.model.transfer.QTransferTransaction;
import org.c4marathon.assignment.domain.model.transfer.TransferTransaction;
import org.springframework.stereotype.Repository;

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

		return queryFactory
			.select(tx)
			.from(tx)
			.join(tx.fromMainAccount, main).fetchJoin()
			.join(main.member, member).fetchJoin()
			.where(
				tx.status.eq(TransferStatus.PENDING),
				tx.expiredAt.loe(remindTime)
			)
			.transform(
				groupBy(member).as(list(tx)) // 결과를 member 단위로 묶음 후 각 그룹의 값으로 TransferTransaction의 리스트를 지정
			);
	}
}
