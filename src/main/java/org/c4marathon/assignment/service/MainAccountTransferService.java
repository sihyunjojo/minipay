package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.account.MainAccount;
import org.c4marathon.assignment.domain.account.enums.AccountPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountTransferService {

	@PersistenceContext
	private EntityManager entityManager;

	private final MainAccountService mainAccountService;
	private final TransferService transferDomainService;

	@Transactional
	public void transfer(Long fromMemberId, Long toMemberId, Long amount) {
		MainAccount from = mainAccountService.getByMemberId(fromMemberId);
		MainAccount to = mainAccountService.getByMemberId(toMemberId);

		Long currentBalance = from.getBalance();
		long shortfall = amount - currentBalance;

		// 잔액 부족 시 자동 충전
		if (shortfall > 0) {
			Long chargeUnit = AccountPolicy.CHARGE_UNIT.getValue();

			// 부족한 금액만큼, 단위에 맞춰 올림 처리해서 충전
			Long chargeAmount = ((shortfall + chargeUnit - 1) / chargeUnit) * chargeUnit;

			mainAccountService.charge(from, chargeAmount);
		}

		// 실제 송금 수행
		transferDomainService.transfer(from, to, amount);
	}

	// MySQL의 Repeatable Read 격리 수준만으로는 부족해.
	// 왜냐면 자바에서 조건을 먼저 판단하고 update를 나중에 할 경우, 그 사이에 **경쟁 조건(race condition)**이 발생할 수 있기 때문이야.
	// @Transactional
	// public void transfer2(Long fromMemberId, Long toMemberId, Long amount) {
	// 	MainAccount from = mainAccountService.getByMemberId(fromMemberId);
	// 	MainAccount to = mainAccountService.getByMemberId(toMemberId);
	//
	// 	Long currentBalance = from.getBalance();
	// 	long shortfall = amount - currentBalance;
	//
	// 	// 잔액 부족 시 자동 충전
	// 	if (shortfall > 0) {
	// 		Long chargeUnit = AccountPolicy.CHARGE_UNIT.getValue();
	//
	// 		// 부족한 금액만큼, 단위에 맞춰 올림 처리해서 충전
	// 		Long chargeAmount = ((shortfall + chargeUnit - 1) / chargeUnit) * chargeUnit;
	//
	// 		mainAccountService.fastCharge(from, chargeAmount);
	// 		entityManager.refresh(from); // 영속성 컨텍스트의 값을 DB 값으로 강제로 덮어쓰기 함
	// 		// from = mainAccountService.getByMemberId(from.getId()); // 💡 최신 상태 보장
	// 	}
	//
	// 	// 실제 송금 수행
	// 	// 내가 들고 있는 from 객체는 여전히 이전의 상태 (그래서 에러)
	// 	// 정합성이 맞지 않아서 많은 트래픽 발생시, 에러 발생
	// 	transferDomainService.transfer(from, to, amount);
	// }

	@Transactional
	public void transfer3(Long fromMemberId, Long toMemberId, Long amount) {
		MainAccount from = mainAccountService.getByMemberId(fromMemberId);
		MainAccount to = mainAccountService.getByMemberId(toMemberId);

		Long currentBalance = from.getBalance();
		long shortfall = amount - currentBalance;

		if (shortfall > 0) {
			Long chargeUnit = AccountPolicy.CHARGE_UNIT.getValue();
			Long chargeAmount = ((shortfall + chargeUnit - 1) / chargeUnit) * chargeUnit;

			// ⚡ DB에서 한도/잔액 조건 포함 충전 시도
			mainAccountService.conditionalFastCharge(
				from.getId(),
				chargeAmount,
				amount
			);

			// getByMemberId() 안에서 JPA가 내부적으로 실행하는 SELECT는, 이미 해당 엔티티(MainAccount)가 같은 트랜잭션의 영속성 컨텍스트에 존재하면,
			// → DB에서 가져오지 않고, 기존 엔티티를 그대로 반환합니다.
			// 그러므로 아래에서 해야함.
			entityManager.refresh(from); // 영속성 컨텍스트의 값을 DB 값으로 강제로 덮어쓰기 함
			// from = mainAccountService.getByMemberId(from.getId()); // 💡 최신 상태 보장
			// ✅ 결론부터 말하면:
			// 항목	entityManager.refresh()	repository.findById() (getById)
			// 속도	✅ 더 빠름 (쿼리 한 번, 객체 재사용)	❌ 약간 느림 (쿼리 + 객체 재생성)
			// 효율성	✅ 기존 객체 유지	새 객체 생성 (기존 객체와 교체 필요)
			// 복잡성	낮음	높음 (객체 대체 코드 필요)
			// 사용처	트랜잭션 내에서 부분 동기화할 때	기존 객체가 무효하거나 detached일 때		}

			// ✅ 이제 balance는 충분히 보장된 상태
			transferDomainService.transfer(from, to, amount);
		}
	}
}
