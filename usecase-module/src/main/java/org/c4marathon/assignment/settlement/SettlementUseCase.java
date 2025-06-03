package org.c4marathon.assignment.settlement;

import java.util.List;

import org.c4marathon.assignment.domain.service.SettlementService;
import org.c4marathon.assignment.settlement.dto.SettlementRequestDto;
import org.c4marathon.assignment.settlement.dto.SettlementResponseDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementUseCase{

	private final SettlementService settlementService;

	public List<SettlementResponseDto> settle(SettlementRequestDto request) {
		return settlementService.calculateSettlement(request);
	}
}
