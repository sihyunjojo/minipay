package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.account.MainAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MainAccountRepository extends JpaRepository<MainAccount, Long>, MainAccountQueryRepository {
	// DB에서 MainAccount 테이블의 모든 컬럼을 가져옵니다.
	// 이걸 다시 JPA가 엔티티로 변환하고 영속성 컨텍스트에 등록하죠.
	// DB IO + 매핑 비용이 큼 → 느림
	@Query("SELECT m FROM MainAccount m WHERE m.member.id = :memberId")
	Optional<MainAccount> findByMemberId(@Param("memberId") Long memberId);

	// DB에서 가져오는 건 딱 id 하나
	@Query("SELECT m.id FROM MainAccount m WHERE m.member.id = :memberId")
	Optional<Long> findIdByMemberId(@Param("memberId") Long memberId);

	// JPA가 자동으로 영속성 컨텍스트를 초기화
	// 이후 findById(id) 같은 조회를 해도 영속성 컨텍스트의 캐시가 아닌, DB의 최신값을 보장
	// 쿼리를 실행한 후, 현재 영속성 컨텍스트(1차 캐시)를 자동으로 초기화(clear) 해주는 역할
	@Modifying(clearAutomatically = true)
	// JPQL 기반의 Bulk Update 쿼리
	// 영속성 컨텍스트를 거치지 않음
	@Query("UPDATE MainAccount m SET m.dailyChargeAmount = 0")
	void resetAllDailyChargeAmount();

	// JPA 직접 업데이트를 쓸 땐 거의 무조건 필수
	@Modifying(clearAutomatically = true)
	// JPA의 영속성 컨텍스트(1차 캐시)를 거치지 않는다 (JPA는 “내 객체가 바뀌었다”는 걸 모름)
	@Query("UPDATE MainAccount m SET m.balance = m.balance + :amount, m.dailyChargeAmount = m.dailyChargeAmount + :amount WHERE m.id = :id")
	// DB에 직접 UPDATE만 하고, 영속성 컨텍스트에는 반영되지 않음
	void fastCharge(@Param("id") Long id, @Param("amount") Long amount);

	int conditionalFastCharge(Long id, Long amount, Long minRequiredBalance, Long dailyLimit);


	// 동시 요청이 발생할 수 있기 때문이야.
	// 애플리케이션에서 from.getBalance()로 확인한 시점과
	// DB에서 UPDATE가 실제로 일어나는 시점 사이에는 시간 차이가 존재해.
	// 그 사이에 다른 트랜잭션이 balance를 바꿔버릴 수 있어.
	// Pre-check in app + assert in DB 패턴 (내가 보기엔 괜찮아 보여도, DB가 최종 판단한다)
	// 포인트, 쿠폰, 돈처럼 정합성이 중요한 도메인에서 거의 필수로 사용
	// 이 조건은 비관적 락보다 가볍다
	// 이 방식을 쓰면 낙관적,비관적락을 안쓰고 그냥 락을 안걸어도 정합성을 유지시켜줌.
// 	@Modifying
// 	@Query("""
//     UPDATE MainAccount m
//     SET m.balance = m.balance + :amount,
//         m.dailyChargeAmount = m.dailyChargeAmount + :amount
//     WHERE m.id = :id
//     AND m.balance + :amount >= :minRequiredBalance
//     AND m.dailyChargeAmount + :amount <= :limit
// """)
// 	int beforeConditionalFastCharge(@Param("id") Long id,
// 		@Param("amount") Long amount,
// 		@Param("minRequiredBalance") Long minRequiredBalance,
// 		@Param("limit") Long limit);


}
