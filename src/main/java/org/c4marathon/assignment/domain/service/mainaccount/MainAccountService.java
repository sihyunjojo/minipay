package org.c4marathon.assignment.domain.service.mainaccount;

import static org.springframework.transaction.annotation.Propagation.*;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.domain.model.Member;
import org.c4marathon.assignment.domain.model.account.MainAccount;
import org.c4marathon.assignment.domain.model.account.enums.AccountPolicy;
import org.c4marathon.assignment.domain.repository.mainaccount.MainAccountRepository;
import org.c4marathon.assignment.domain.service.AccountPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainAccountService {

	private static final int MAX_RETRY = 20;
	private static final long BASE_SLEEP_TIME_MS = 50;      // 초기 대기 시간
	private static final long MAX_SLEEP_TIME_MS = 1500;     // 최대 대기 시간

	private final MainAccountRepository mainAccountRepository;
	private final AccountPolicyService accountPolicyService;

	@Transactional
	public void createMainAccountForMember(Member member) {
		boolean accountExists = mainAccountRepository.findByMemberId(member.getId()).isPresent();
		if (accountExists) {
			throw new IllegalStateException("회원이 이미 메인 계좌를 가지고 있습니다.");
		}

		MainAccount mainAccount = MainAccount.builder().balance(0L).build();

		member.setMainAccount(mainAccount);
		mainAccountRepository.save(mainAccount);
	}

	// @Transactional(readOnly = true)를 쓰면 무조건 빨라진다고 오해하지만,
	// 실제로는 JPA의 변경 감지(dirty checking)를 비활성화하는 정도
	@Transactional(readOnly = true)
	public Long getMainAccountByMemberId(Long memberId) {
		return mainAccountRepository.findIdByMemberId(memberId)
			.orElseThrow(() -> new IllegalStateException("메인 계좌가 존재하지 않습니다."));
	}

	// 📌 2. ID 기반 최신 조회 (Post-fetch용)
	public MainAccount findById(Long id) {
		return mainAccountRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("ID가 " + id + "인 메인 계좌가 존재하지 않습니다."));
	}

	@Transactional
	public void resetAllDailyChargeAmount() {
		mainAccountRepository.resetAllDailyChargeAmount();
	}

	@Transactional(readOnly = true)
	public Long calculateShortfall(Long accountId, Long transferAmount) {
		Long currentBalance = mainAccountRepository.findMainAccountAmountById(accountId);

		return transferAmount - currentBalance;
	}

	@Transactional()
	public void chargeOrThrow(Long accountId, Long shortfall, Long minRequiredBalance) {
		// Assert in DB (DB 정합성 확인하며 충전 시도)
		long chargeAmount = accountPolicyService.getRoundedCharge(shortfall);

		boolean success = mainAccountRepository.tryFastCharge(accountId, chargeAmount, minRequiredBalance,
			accountPolicyService.getPolicyValue(AccountPolicy.MAIN_DAILY_LIMIT));

		if (!success) {
			throw new IllegalStateException("충전 불가: 충전해도 잔액 부족이거나 일일 한도 초과");
		}
	}

	// 성능보다 정합성이 우선되어야함.
	// 재전송 로직을 통한
	@Transactional
	public void transfer(Long fromAccountId, Long toAccountId, Long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("송금 금액은 0보다 커야 합니다.");
		}
		if (fromAccountId.equals(toAccountId)) {
			throw new IllegalArgumentException("자신에게 송금할 수 없습니다.");
		}

		MainAccount from = findById(fromAccountId); // 낙관적
		MainAccount to = findById(toAccountId); // 낙관적

		withdrawWithRetry(fromAccountId, amount, from);
		depositWithRetry(toAccountId, amount, to);
	}

	private void withdrawWithRetry(Long accountId, Long amount, MainAccount account) {
		int retryCount = 0;
		while (retryCount++ < MAX_RETRY) {
			if (mainAccountRepository.withdrawByOptimistic(accountId, amount, account.getVersion()) > 0) {
				log.debug("출금 성공, retryCount: {}", retryCount);
				return;
			}
			if (retryCount == MAX_RETRY) {
				throw new OptimisticLockException("동시성 충돌로 인해 출금에 실패했습니다.");
			}
			account = findById(accountId);
			sleepWithBackoff(retryCount);
		}
	}

	private void depositWithRetry(Long accountId, Long amount, MainAccount account) {
		int retryCount = 0;
		while (retryCount++ < MAX_RETRY) {
			if (mainAccountRepository.depositByOptimistic(accountId, amount, account.getVersion()) > 0) {
				log.debug("입금 성공, retryCount: {}", retryCount);
				return;
			}
			if (retryCount == MAX_RETRY) {
				throw new OptimisticLockException("동시성 충돌로 인해 입금에 실패했습니다.");
			}
			account = findById(accountId);
			sleepWithBackoff(retryCount);
		}
	}

	private void sleepWithBackoff(int retryCount) {
		try {
			long sleepTime = Math.min(BASE_SLEEP_TIME_MS * (1L << (retryCount - 1)), MAX_SLEEP_TIME_MS);
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("스레드 인터럽트 발생", e);
		}
	}

}
