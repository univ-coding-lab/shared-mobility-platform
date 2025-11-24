# 공유 모빌리티 플랫폼 - 문서

공유 모빌리티 플랫폼 마이크로서비스 아키텍처에 대한 완전한 기술 문서입니다.

## 📚 문서 목록

### UML 다이어그램

#### 클래스 다이어그램
- [**종합 클래스 다이어그램**](diagrams/class-diagram-comprehensive.puml) (PlantUML)
  - 모든 도메인 엔티티와 관계
  - 이벤트 계층 구조 (BaseEvent → 도메인 이벤트)
  - Saga 패턴 클래스
  - 서비스 계층, 리포지토리, 인터페이스
  - **보는 방법**: IntelliJ/VS Code의 PlantUML 플러그인 또는 [PlantUML Online](https://www.plantuml.com/plantuml/uml/)

#### 시퀀스 다이어그램 (Mermaid)
- [**차량 대여 플로우**](diagrams/sequence-vehicle-rental.md)
  - 완전한 대여 생성 흐름
  - Saga 오케스트레이션 단계
  - Kafka 이벤트 발행 및 소비
  - 병렬 이벤트 처리 (Vehicle + Location 서비스)

- [**차량 반납 및 결제 플로우**](diagrams/sequence-vehicle-return.md)
  - 대여 완료 및 요금 계산
  - 다중 서비스 이벤트 전파
  - 배터리 모니터링 통합

- [**실시간 위치 및 배터리 모니터링**](diagrams/sequence-realtime-monitoring.md)
  - IoT 텔레메트리 데이터 흐름
  - GPS 위치 추적
  - 배터리 알림 및 저배터리 처리
  - Redis 캐싱 전략

#### 아키텍처 다이어그램 (Mermaid)
- [**아키텍처 개요**](diagrams/architecture-overview.md)
  - C4 모델: System Context, Container, Component 뷰
  - 기술 스택 분석
  - 네트워크 토폴로지 및 포트
  - 배포 아키텍처 (Docker Compose)
  - 확장성 및 고가용성 전략

#### 콜레보레이션 다이어그램
- [**차량 대여 플로우 협력 다이어그램**](diagrams/collaboration-rental-flow.puml) (PlantUML)
  - 객체 상호작용 및 메시지 전달
  - 동기 vs 비동기 통신
  - 병렬 이벤트 처리 경로

---

### 디자인 패턴
- [**디자인 패턴 문서 (한글)**](design-patterns-ko.md) | [English Version](design-patterns.md)
  - **아키텍처 패턴**: Microservices, Event-Driven, Database-per-Service, Saga, Hexagonal
  - **생성 패턴**: Builder, Factory Method, Singleton
  - **구조 패턴**: Facade, Proxy, Adapter
  - **행위 패턴**: Observer, Template Method, Chain of Responsibility, Strategy
  - **Spring 패턴**: Dependency Injection, Repository, Service Layer
  - **통합 패턴**: Idempotent Consumer, Event Sourcing, Compensating Transaction
  - **동시성 패턴**: Thread Pool, Optimistic Locking

  각 패턴은 다음을 포함합니다:
  - 목적 및 근거
  - 구현 세부사항
  - 파일 경로가 포함된 코드 예제
  - 장점 및 트레이드오프

---

### 아키텍처 의사결정
- [**아키텍처 의사결정 기록 (ADR) - 한글**](architecture-decisions-ko.md) | [English Version](architecture-decisions.md)
  - ADR-001: 마이크로서비스 아키텍처
  - ADR-002: Kafka를 사용한 이벤트 드리븐 아키텍처
  - ADR-003: Database Per Service 패턴
  - ADR-004: 분산 트랜잭션을 위한 Saga 패턴
  - ADR-005: 시계열 데이터를 위한 MongoDB
  - ADR-006: 캐싱 및 Saga 상태를 위한 Redis
  - ADR-007: Idempotent Consumer 패턴
  - ADR-008: IoT 통합을 위한 Hexagonal 아키텍처
  - ADR-009: 마이크로서비스를 위한 Spring Boot
  - ADR-010: 인증을 위한 JWT
  - ADR-011: 로컬 개발을 위한 Docker Compose
  - ADR-012: 수동 Kafka Acknowledgment
  - ADR-013: 낙관적 잠금 (Optimistic Locking)

---

## 🎯 빠른 시작 가이드

### 다이어그램 보기

**Mermaid 다이어그램** (시퀀스, 아키텍처):
- GitHub/GitLab: 마크다운 파일에서 자동 렌더링
- VS Code: "Markdown Preview Mermaid Support" 확장 설치
- IntelliJ: "Mermaid" 플러그인 설치
- 온라인: [Mermaid Live Editor](https://mermaid.live/)에 복사-붙여넣기

**PlantUML 다이어그램** (클래스, 콜레보레이션):
- IntelliJ IDEA: "PlantUML Integration" 플러그인 설치 → `.puml` 파일 우클릭 → "View Diagram"
- VS Code: "PlantUML" 확장 설치 → `Cmd/Ctrl+Shift+P` → "PlantUML: Preview Current Diagram"
- 온라인: [PlantUML Online](https://www.plantuml.com/plantuml/uml/)에 복사-붙여넣기

---

## 🏗️ 시스템 아키텍처 개요

### 마이크로서비스

| 서비스 | 포트 | 데이터베이스 | 목적 |
|---------|------|----------|---------|
| **User Service** | 8081 | PostgreSQL (5432) | 인증, 사용자 프로필 |
| **Vehicle Service** | 8082 | PostgreSQL (5433) | 차량 재고, IoT 통합 |
| **Rental Service** | 8083 | PostgreSQL (5434) | 대여 트랜잭션, Saga 오케스트레이션 |
| **Location Service** | 8084 | MongoDB (27017) | GPS 추적, 지리공간 쿼리 |
| **Battery Service** | 8085 | MongoDB (27018) | 배터리 모니터링, 알림 |

### 인프라

| 컴포넌트 | 포트 | 목적 |
|-----------|------|---------|
| **Redis** | 6379 | Saga 상태, 위치 캐시, 멱등성 |
| **Kafka** | 9092 | 이벤트 스트리밍 백본 |
| **Kafka UI** | 8090 | Kafka 모니터링 대시보드 |
| **Zookeeper** | 2181 | Kafka 클러스터 조정 |

### 기술 스택

- **백엔드**: Spring Boot 3.x, Java 17
- **데이터베이스**: PostgreSQL 14, MongoDB 6, Redis 7
- **메시징**: Apache Kafka 3.x
- **컨테이너화**: Docker, Docker Compose
- **인증**: JWT (Spring Security)
- **ORM**: Spring Data JPA (PostgreSQL), Spring Data MongoDB

---

## 📊 주요 지표

### 이벤트 토픽 (총 14개)
- 차량 이벤트: `vehicle.rented`, `vehicle.returned`, `vehicle.moved`, `vehicle.status.changed`
- 배터리 이벤트: `battery.low`, `battery.critical`, `battery.updated`
- 대여 이벤트: `rental.started`, `rental.completed`, `rental.cancelled`
- 결제 이벤트: `payment.completed`, `payment.failed`
- 유지보수 이벤트: `maintenance.required`, `maintenance.completed`
- 위치 이벤트: `location.updated`

### 데이터베이스 스키마
- **PostgreSQL**: 3개 데이터베이스 (users, vehicles, rentals)
- **MongoDB**: 2개 데이터베이스 (location_logs, battery_logs)
- **Redis**: 3가지 용도 (saga state, cache, idempotency)

---

## 🔍 문서 탐색 방법

### 신입 개발자용
1. [아키텍처 개요](diagrams/architecture-overview.md)로 시작하여 시스템 구조 이해
2. [디자인 패턴](design-patterns.md)을 읽고 사용된 코딩 패턴 학습
3. [시퀀스 다이어그램](diagrams/sequence-vehicle-rental.md)을 학습하여 요청 흐름 이해

### 아키텍트용
1. [아키텍처 의사결정](architecture-decisions.md)을 검토하여 기술 선택의 근거 파악
2. [클래스 다이어그램](diagrams/class-diagram-comprehensive.puml)으로 도메인 모델 학습
3. [아키텍처 개요](diagrams/architecture-overview.md)를 분석하여 확장성 전략 이해

### 운영/DevOps용
1. [아키텍처 개요](diagrams/architecture-overview.md)에서 배포 토폴로지 확인
2. Docker Compose 설정 검토 (`/docker-compose.yml`)
3. `http://localhost:8090`에서 Kafka UI 모니터링

### QA/테스터용
1. [시퀀스 다이어그램](diagrams/sequence-vehicle-rental.md)을 학습하여 테스트 시나리오 이해
2. 시퀀스 다이어그램에서 에러 처리 검토
3. [디자인 패턴](design-patterns.md)에서 멱등성 및 재시도 로직 확인

---

## 🛠️ 개발 워크플로우

### 1. 인프라 시작
```bash
docker-compose up -d
```

### 2. 서비스 확인
```bash
# 서비스 헬스 체크
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Vehicle Service
curl http://localhost:8083/actuator/health  # Rental Service

# Kafka 토픽 확인
open http://localhost:8090  # Kafka UI
```

### 3. 대여 플로우 테스트
```bash
# 1. 사용자 등록
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com", "password":"password123", "firstName":"John", "lastName":"Doe"}'

# 2. 로그인
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com", "password":"password123"}'

# 3. 대여 시작
curl -X POST http://localhost:8083/rentals/start \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"userId":"...", "vehicleId":"...", "startLatitude":37.7749, "startLongitude":-122.4194}'

# 4. Kafka UI에서 이벤트 확인
open http://localhost:8090
```

---

## 📖 관련 문서

### 코드 문서
- JavaDoc: `./gradlew javadoc`로 생성 (각 서비스별)
- API 문서: `http://localhost:808x/swagger-ui.html`의 Swagger UI (활성화된 경우)

### 프로젝트 파일
- `/docker-compose.yml`: 인프라 설정
- `/services/*/src/main/resources/application.yml`: 서비스 설정
- `/common/common-domain/`: 공유 도메인 모델
- `/common/common-event/`: 이벤트 정의 및 Kafka 설정

### 외부 리소스
- [Spring Boot 문서](https://spring.io/projects/spring-boot)
- [Apache Kafka 문서](https://kafka.apache.org/documentation/)
- [MongoDB 매뉴얼](https://docs.mongodb.com/manual/)
- [Redis 문서](https://redis.io/documentation)

---

## 🔄 문서 유지보수

### 버전 이력
| 버전 | 날짜 | 작성자 | 변경사항 |
|---------|------|--------|---------|
| 1.0 | 2025-01-24 | 개발팀 | 초기 문서 생성 |

### 검토 일정
- **분기별**: ADR 관련성 검토
- **주요 릴리스마다**: 플로우 변경 시 시퀀스 다이어그램 업데이트
- **스키마 변경마다**: 클래스 다이어그램 업데이트

### 기여 방법
1. 새 기능 추가 시 다이어그램 업데이트
2. 중요한 아키텍처 변경 시 ADR 추가
3. 패턴 카탈로그를 새로운 패턴으로 업데이트
4. 모든 Mermaid/PlantUML 다이어그램이 올바르게 렌더링되는지 테스트

---

## ❓ FAQ

**Q: 왜 두 가지 유형의 다이어그램(Mermaid와 PlantUML)이 있나요?**
A: GitHub에서 렌더링되는 간단한 다이어그램(시퀀스, 아키텍처)은 Mermaid를 사용합니다. 더 많은 기능이 필요한 복잡한 다이어그램(클래스, 콜레보레이션)은 PlantUML을 사용합니다.

**Q: 새 마이크로서비스를 어떻게 추가하나요?**
A: 기존 구조를 따라 서비스 모듈 생성, `docker-compose.yml`에 추가, 아키텍처 다이어그램 업데이트, 결정에 대한 ADR 작성하면 됩니다.

**Q: API 요청 예제는 어디서 찾을 수 있나요?**
A: 시퀀스 다이어그램에서 요청/응답 예제를 확인하거나 Postman 컬렉션(사용 가능한 경우)을 사용하세요.

**Q: 이벤트 흐름을 어떻게 디버깅하나요?**
A: `http://localhost:8090`의 Kafka UI를 사용하여 실시간으로 이벤트를 확인하고, 컨슈머 지연을 체크하며, 메시지 페이로드를 검사하세요.

---

**질문이나 설명이 필요하면 개발팀에 문의하세요.**

---

## 📋 새 기능 추가 시 체크리스트

- [ ] 새 엔티티 추가 시 클래스 다이어그램 업데이트
- [ ] 새 플로우 도입 시 시퀀스 다이어그램 생성
- [ ] 새 패턴 사용 시 디자인 패턴 문서 업데이트
- [ ] 아키텍처 결정 시 ADR 작성
- [ ] 새 문서 링크로 README 업데이트
- [ ] 모든 다이어그램이 올바르게 렌더링되는지 테스트
- [ ] API 문서 업데이트 (Swagger)
- [ ] 새 플로우에 대한 통합 테스트 추가

---

**즐거운 코딩 되세요! 🚀**
