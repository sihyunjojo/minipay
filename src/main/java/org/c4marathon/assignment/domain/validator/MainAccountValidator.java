package org.c4marathon.assignment.domain.validator;

import java.util.List;

import org.c4marathon.assignment.dto.account.SettlementRequestDto;
import org.springframework.stereotype.Component;

@Component
public class MainAccountValidator {

	public void validateSettlement(SettlementRequestDto request) {
		List<Long> participantIds = request.participantMemberIdList();
		long totalAmount = request.totalAmount();

		if (totalAmount <= 0) {
			throw new IllegalArgumentException("정산 금액은 0보다 커야 합니다.");
		}
		if (participantIds == null || participantIds.isEmpty()) {
			throw new IllegalStateException("참여자 목록이 비어있을 수 없습니다.");
		}
		if (participantIds.stream().distinct().count() != participantIds.size()) {
			throw new IllegalStateException("참여자 목록에 중복된 ID가 존재합니다.");
		}
	}

	public void validateTransfer(Long fromId, Long toId, Long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
		}
		if (fromId.equals(toId)) {
			throw new IllegalArgumentException("자신에게 송금할 수 없습니다.");
		}
	}
}
