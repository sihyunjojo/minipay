package org.c4marathon.assignment.domain.repository.transfertransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.account.QMainAccount;
import org.c4marathon.assignment.domain.model.transfer.enums.TransferStatus;
import org.c4marathon.assignment.domain.model.member.Member;
import org.c4marathon.assignment.domain.model.member.QMember;
import org.c4marathon.assignment.domain.model.transfer.QPendingTransferTransaction;
import org.c4marathon.assignment.domain.model.transfer.PendingTransferTransaction;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
@RequiredArgsConstructor
public class PendingTransferTransactionQueryRepositoryImpl implements PendingTransferTransactionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * @param remindTime 기준 시간
     * @return 회원별 PENDING 만료 대상 트랜잭션 맵
     */
    @Override
    public Map<Member, List<PendingTransferTransaction>> findRemindTargetGroupedByMember(LocalDateTime remindTime) {
        QPendingTransferTransaction tx = QPendingTransferTransaction.pendingTransferTransaction;
        QMainAccount main = QMainAccount.mainAccount;
        QMember member = QMember.member;
        // Implement query logic here...
        // This is a stub. Fill in with actual QueryDSL logic as needed.
        return null;
    }
}
