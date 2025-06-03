package org.c4marathon.assignment.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.c4marathon.assignment.domain.model.PendingTransfer;
import org.c4marathon.assignment.infra.persistence.jpa.JpaPendingTransferRepository;
import org.c4marathon.assignment.infra.persistence.query.PendingTransferQueryRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PendingTransferRepositoryImpl implements PendingTransferRepository {

	private final JpaPendingTransferRepository jpa;
	private final PendingTransferQueryRepository query;

	@Override
	public Optional<PendingTransfer> findPendingById(Long id) {
		return jpa.findPendingPendingTransferById(id);
	}

	@Override
	public List<PendingTransfer> findRemindTargetsWithMainAccount(LocalDateTime time) {
		return jpa.findRemindPendingTargetTransactionsWithMainAccount(time);
	}

	@Override
	public List<PendingTransfer> findRemindTargetsWithMember(LocalDateTime time) {
		return jpa.findRemindPendingTargetTransactionsWithMember(time);
	}

	@Override
	public Map<Member, List<PendingTransfer>> findRemindTargetGroupedByMember(LocalDateTime remindTime) {
		return query.findRemindTargetGroupedByMember(remindTime);
	}}
