package org.c4marathon.assignment.usecase;

import java.util.List;

import org.c4marathon.assignment.domain.service.mainaccount.SettlementService;
import org.c4marathon.assignment.domain.validator.MainAccountValidator;
import org.c4marathon.assignment.dto.account.SettlementRequestDto;
import org.c4marathon.assignment.dto.account.SettlementResponseDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementUseCase{

	private final MainAccountValidator mainAccountValidator;
	private final SettlementService settlementService;

	public List<SettlementResponseDto> settle(SettlementRequestDto request) {
		mainAccountValidator.validateSettlement(request);
		return settlementService.calculateSettlement(request);
	}
}
