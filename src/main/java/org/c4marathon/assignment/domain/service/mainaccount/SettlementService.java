package org.c4marathon.assignment.domain.service.mainaccount;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import org.c4marathon.assignment.domain.model.policy.ExternalAccountPolicy;
import org.c4marathon.assignment.dto.sattlement.SettlementRequestDto;
import org.c4marathon.assignment.dto.sattlement.SettlementResponseDto;
import org.springframework.stereotype.Service;

@Service
public class SettlementService {

	public static final Long COMPANY_ID = ExternalAccountPolicy.COMPANY.getId();

	public List<SettlementResponseDto> calculateSettlement(SettlementRequestDto request) {
		List<Long> participantIds = request.participantMemberIdList();
		long totalAmount = request.totalAmount();

		return switch (request.type()) {
			case EQUAL -> calculateEqualShare(totalAmount, participantIds);
			case RANDOM -> calculateRandomShare(totalAmount, participantIds);
			default -> throw new IllegalStateException(String.format("%s는 존재하지 않는 값입니다. ", request.type()));
		};
	}

	// 1/n 후 나머지 값은 타 페이 서비스와 같이 회사에서 지원하는 방식
	private List<SettlementResponseDto> calculateEqualShare(long total, List<Long> participantMemberIdList) {
		int count = participantMemberIdList.size();
		long base = total / count;
		long remainder = total % count;

		List<SettlementResponseDto> responses = new ArrayList<>();
		for (Long participantId : participantMemberIdList) {
			responses.add(new SettlementResponseDto(participantId, base));
		}

		// 남은 금액은 회사(-1)에서 부담
		if (remainder > 0) {
			responses.add(new SettlementResponseDto(COMPANY_ID, remainder));
		}

		return responses;
	}

	private List<SettlementResponseDto> calculateRandomShare(long total, List<Long> participantMemberIdList) {
		int count = participantMemberIdList.size();
		SplittableRandom rand = new SplittableRandom();
		List<Long> shares = new ArrayList<>();
		long remaining = total;

		for (int i = 0; i < count - 1; i++) {
			long max = remaining - (count - i - 1);
			long amount = 1 + rand.nextLong(max);
			shares.add(amount);
			remaining -= amount;
		}
		shares.add(remaining);

		List<SettlementResponseDto> responses = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			responses.add(new SettlementResponseDto(participantMemberIdList.get(i), shares.get(i)));
		}

		return responses;
	}
}
