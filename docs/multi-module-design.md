# Mini-Pay 프로젝트 아키텍처 설계 (Hybrid + 도메인 중심 멀티모듈)

## ✅ 1. 전체 구조 설계도
```
mini-pay/
├── app-api             // main, config, swagger, controller routing
├── domain-member       // 사용자/계좌 도메인 (회원가입, 계좌 생성/조회 등)
├── domain-transfer     // 송금 도메인
├── domain-settlement   // 정산 도메인
├── domain-savings      // 적금 도메인
├── domain-history      // 송금 로그/이력 도메인
├── infra-kafka         // Kafka 메시지 발행/구독
├── infra-redis         // Redis 캐시, 충전 한도 등
├── infra-scheduler     // 배치 스케줄링 (적금 이자, 자동 적금 이체 등)
├── common              // 공통 예외, 응답, 유틸, 공통 어노테이션
└── build.gradle.kts
```

---

## ✅ 2. 모듈별 책임 정리

| 모듈 | 책임 |
|------|------|
| **app-api** | 외부 API Controller, Swagger 설정 |
| **domain-member** | 사용자/계좌, 1일 출금 한도 로직 |
| **domain-transfer** | 송금, 충전, Pending 처리, 동시성 처리 |
| **domain-settlement** | 정산 요청/확정, 1/n 랜덤 분배 처리 |
| **domain-savings** | 정기/자유 적금, 이자 지급 로직 |
| **domain-history** | 송금/입출금 로그 저장 및 조회 |
| **infra-redis** | Redis 캐싱 (충전한도, Pending 상태 등) |
| **infra-kafka** | Kafka 메시지 발행/구독 |
| **infra-scheduler** | Quartz/스프링 배치 기반 자동 실행 로직 |
| **common** | 공통 예외, BaseResponse, CustomException, TimeUtils 등 |

---

## ✅ 3. 의존성 구조 (클린 아키텍처 원칙 기반)

```
[ app-api ]
      ↓
[ domain-* ] → interface만 선언
      ↓
[ infra-* ] → 구현체로 인터페이스 구현
```

- 도메인 모듈은 기술(Infra)에 절대 의존하지 않음
- 인프라 모듈이 도메인의 Port를 구현하는 Adapter 역할

---

## ✅ 4. 핵심 구현 설계 방향

### 🔸 Redis 사용 예시 (1일 충전 한도 관리)

**domain-member**
```java
public interface RedisChargeLimitPort {
    int getTodayChargeAmount(Long memberId);
    void increaseTodayChargeAmount(Long memberId, int amount);
}
```

**infra-redis**
```java
@Component
public class RedisChargeLimitAdapter implements RedisChargeLimitPort {
    // Redis 연동 로직 구현
}
```

---

### 🔸 Scheduler 사용 예시 (적금 이자 지급)

**domain-savings**
```java
public interface InterestSchedulerPort {
    void scheduleInterestPayment();
}
```

**infra-scheduler**
```java
@Component
public class QuartzInterestScheduler implements InterestSchedulerPort {
    @Scheduled(cron = "0 0 4 * * *")
    public void scheduleInterestPayment() {
        // domain-savings 서비스 호출
    }
}
```

---

## ✅ 5. Gradle 의존성 구조 예시

**settings.gradle.kts**
```kotlin
include(
    ":app-api",
    ":domain-member",
    ":domain-transfer",
    ":domain-settlement",
    ":domain-savings",
    ":domain-history",
    ":infra-redis",
    ":infra-kafka",
    ":infra-scheduler",
    ":common"
)
```

**app-api/build.gradle.kts**
```kotlin
dependencies {
    implementation(project(":domain-member"))
    implementation(project(":domain-transfer"))
    implementation(project(":domain-settlement"))
    implementation(project(":domain-savings"))
    implementation(project(":domain-history"))
    implementation(project(":common"))
}
```

**domain-transfer/build.gradle.kts**
```kotlin
dependencies {
    implementation(project(":infra-redis"))
    implementation(project(":infra-kafka"))
    implementation(project(":common"))
}
```

---

## ✅ 6. 이 구조가 Mini-Pay에 적합한 이유

| 기능 | 아키텍처 적용 방식 |
|------|-------------------|
| 1일 충전 한도 Redis 캐싱 | infra-redis에서 관리, domain-member는 포트만 사용 |
| 송금 Pending 상태 관리 | Redis + DB, 포트 추상화 처리 |
| 대용량 로그 처리 | domain-history, 쿼리 성능/인덱스 설계 가능 |
| 이자 정산/자동이체 스케줄링 | infra-scheduler 일괄 관리 |
| 성능 테스트 | 도메인 단위 부하 테스트, 모듈별 비교 |
| MSA 전환 | 도메인 모듈 → 서비스 단위로 확장 가능 |

---

## ✅ 7. 최종 요약 및 장점

- **비즈니스 로직**은 `domain-xxx` 모듈에서 책임지고,
- **기술 요소**는 `infra-xxx` 모듈에서 통합 관리

💡 클린 아키텍처 적용 → 유지보수, 확장성, 테스트 최적화  
💡 도메인 응집도 극대화 + 기술 재사용성 확보  
💡 MSA 전환, 스케줄링, 캐시, 메시징, 로그 분석 대응 가능

---

## 🔧 다음 단계 제안

1. 도메인-모듈 목록 확정
2. 각 도메인별 책임 명확화 (엔티티, 유스케이스, 인터페이스)
3. 포트/어댑터 구조 구현
4. Gradle 설정 및 멀티모듈 적용
5. 공통 예외 및 응답 포맷 구조 정의
