# Mini-Pay
[취뽀컴트루 & 취준마라톤 과제 레포](https://github.com/sihyunjojo/c4-cometrue-assignment)를 기반으로 개발한 프로젝트입니다.

# 프로젝트 소개

이 프로젝트는 계좌 이체 및 보류 이체 기능을 제공하는 금융 서비스 백엔드입니다.  
주요 기능으로는 일반 계좌 간 이체, 보류 이체(수취인 승인 후 이체 완료), 이체 취소 등이 있습니다.

---


## 주요 기능

### 1. 일반 이체
- 실시간 계좌 이체 (메인계좌 → 메인계좌, 메인계좌 → 적금계좌)
- 트랜잭션 관리와 동시성 제어를 통한 안전한 자산 이체
- 낙관적 락(Optimistic Lock)을 활용한 동시성 제어

### 2. 보류 이체
- 수신자 확인이 필요한 보류 이체 기능
- 보류 이체 수락/거절/취소 기능
- 미확인 보류 이체에 대한 자동 만료 처리

### 3. 이체 내역
- 사용자별 이체 내역 조회
- 커서 기반 페이지네이션을 통한 대량 데이터 효율적 조회
- 다양한 조건에 따른 필터링 및 정렬

---

# 프로젝트 구조

```
c4-cometrue-assignment/
cometrue-assignment/
├── interface-module/      # API 계층
├── transfer-module/       # 이체 도메인
├── transfer-log-module/   # 이체 내역 도메인
├── main-account-module/   # 메인 계좌 도메인
├── saving-account-module/ # 적금 계좌 도메인
└── common-module/         # 공통 유틸리티
└── ...
```

---

## 기술적 고민과 해결

### 1. 트랜잭션 관리
- **도전**: 이체 중 예외 발생 시 데이터 일관성 유지  
- **해결**: TransactionTemplate을 사용한 명시적 트랜잭션 관리  
  - `PROPAGATION_REQUIRES_NEW`로 독립 트랜잭션 구성  
  - 예외 발생 시 명시적 롤백 처리

### 2. 동시성 제어
- **도전**: 계좌 잔액 동시 업데이트 시 경쟁 조건  
- **해결**: Optimistic Locking 전략 구현  
  - `@Version`을 사용한 버전 관리  
  - 낙관적 락 충돌 시 `OptimisticLockingFailureException` 처리

### 3. 보류 이체 상태 관리
- **도전**: 복잡한 보류 이체 생명주기 관리  
- **해결**: 상태 패턴(State Pattern) 적용  
  - `PENDING → COMPLETED/CANCELED/EXPIRED` 상태 전이  
  - 각 상태별 비즈니스 로직 캡슐화

### 4. 대량 데이터 페이징
- **도전**: 대량의 이체 내역에서 효율적인 페이징 처리  
- **해결**: 커서 기반 페이지네이션 구현  
  - `WHERE sendTime > lastSeenTime AND id > lastSeenId` 방식의 복합 커서  
  - 인덱스 스캔 최소화를 위한 복합 인덱스 설계

### 5. 커서 기반 페이지네이션
- **도전**: 대용량 이체 로그에서 Offset 방식은 Limit + Offset에 따른 불필요한 정렬/스캔 비용으로 인해 성능 저하 발생
- **해결**: No-Offset 방식의 커서 기반 페이지네이션 구현
  - (sendTime, id) 복합 커서를 기준으로 조회
  - 정렬 기준과 커서 조건을 분리하지 않고 복합 인덱스로 활용
  - limit + 1 방식으로 다음 페이지 존재 여부(hasNext) 판단

### 6. 도메인 모듈화
- **도전**: 도메인 간 의존도가 높아질수록 비즈니스 변경 시 파급 효과가 커지고 유지보수성 저하
- **해결**: 기능 단위 모듈 분리를 통해 응집도와 독립성 강화
  - transfer, main-account, saving-account, transfer-log 등 각각 독립 책임을 가진 모듈로 분리
  - common-module에 공통 로직과 유틸리티 클래스 집중
  - 모듈 간 의존성 최소화로 각 도메인의 독립적인 테스트 및 배포 가능


---

## 성능 최적화

### 1. 쿼리 최적화
- QueryDSL을 활용한 동적 쿼리 구성
- 필요한 컬럼만 선택적으로 조회
- 적절한 인덱스 설계

### 2. 부하 테스트
- **도구**: Locust
- **목표**: 초당 600 TPS 처리  
- **결과**: 평균 응답 시간 200ms 이하 유지
- 
---

# 기타

- **공통 응답 구조**: `ApiResponse`를 통해 일관된 API 응답 제공  
- **Swagger (OpenAPI)**: 각 API에 `@Operation` 어노테이션으로 문서화  
- **Validation**: DTO에 `@Valid` 적용, 컨트롤러 단에서 유효성 검증
