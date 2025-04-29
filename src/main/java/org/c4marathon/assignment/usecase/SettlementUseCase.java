package org.c4marathon.assignment.usecase;

import java.util.List;

import org.c4marathon.assignment.domain.service.mainaccount.SettlementService;
import org.c4marathon.assignment.dto.sattlement.SettlementRequestDto;
import org.c4marathon.assignment.dto.sattlement.SettlementResponseDto;
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
