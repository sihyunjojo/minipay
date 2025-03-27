package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountSchedulerService {

	private final MainAccountRepository mainAccountRepository;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void resetDailyChargeLimits() {
		mainAccountRepository.resetAllDailyChargeAmount();
		log.info("✅ 모든 사용자 일일 충전 한도 초기화 완료");
	}
}
