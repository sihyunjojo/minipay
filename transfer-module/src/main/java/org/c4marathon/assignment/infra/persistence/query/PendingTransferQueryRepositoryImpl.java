package org.c4marathon.assignment.infra.persistence.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.model.PendingTransfer;import org.c4marathon.assignment.domain.model.member.Member;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
@RequiredArgsConstructor
public class PendingTransferQueryRepositoryImpl implements PendingTransferQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * @param remindTime 기준 시간
     * @return 회원별 PENDING 만료 대상 트랜잭션 맵
     */
    @Override
    public Map<Member, List<PendingTransfer>> findRemindTargetGroupedByMember(LocalDateTime remindTime) {
        QPendingTransfer tx = QPendingTransfer.pendingTransferTransaction;
        QMainAccount main = QMainAccount.mainAccount;
        QMember member = QMember.member;
        // Implement query logic here...
        // This is a stub. Fill in with actual QueryDSL logic as needed.
        return null;
    }
}
