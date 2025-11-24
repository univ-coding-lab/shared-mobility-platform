# 공유 모빌리티 플랫폼의 디자인 패턴

이 문서는 프로젝트에서 사용된 모든 디자인 패턴을 분류하여 정리합니다.

> **참고**: 원본 영문 문서는 `design-patterns.md`에서 확인할 수 있습니다.
> 이 문서는 한글 요약 버전으로, 핵심 내용과 코드 예제를 포함합니다.

---

## 📑 목차

1. [아키텍처 패턴](#아키텍처-패턴)
2. [생성 패턴](#생성-패턴)
3. [구조 패턴](#구조-패턴)
4. [행위 패턴](#행위-패턴)
5. [Spring 프레임워크 패턴](#spring-프레임워크-패턴)
6. [엔터프라이즈 통합 패턴](#엔터프라이즈-통합-패턴)

---

## 아키텍처 패턴

### 1. 마이크로서비스 아키텍처 (Microservices Architecture)

**목적**: 애플리케이션을 독립적으로 배포 가능한 서비스로 분해하여 각 서비스가 자체 데이터와 비즈니스 로직을 소유하도록 합니다.

**구현**:
```
services/
├── user-service/       (Port 8081, PostgreSQL)
├── vehicle-service/    (Port 8082, PostgreSQL)
├── rental-service/     (Port 8083, PostgreSQL)
├── location-service/   (Port 8084, MongoDB)
└── battery-service/    (Port 8085, MongoDB)
```

**장점**:
- 독립적인 확장 (Location Service는 20개 인스턴스, 나머지는 5개)
- 기술 다양성 (트랜잭션용 JPA, 시계열용 MongoDB)
- 장애 격리 (Battery Service 다운 시에도 대여는 작동)

---

### 2. 이벤트 드리븐 아키텍처 (Event-Driven Architecture)

**목적**: 비동기 이벤트 전파를 통해 서비스 간 느슨한 결합을 가능하게 합니다.

**Kafka 토픽** (14개):
- 차량: `vehicle.rented`, `vehicle.returned`, `vehicle.moved`, `vehicle.status.changed`
- 배터리: `battery.low`, `battery.critical`, `battery.updated`
- 대여: `rental.started`, `rental.completed`, `rental.cancelled`
- 결제: `payment.completed`, `payment.failed`

**발행자 예제**:
```java
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Override
    public void publish(String topic, String key, DomainEvent event) {
        kafkaTemplate.send(topic, key, event);
    }
}
```

**소비자 예제**:
```java
@KafkaListener(topics = KafkaTopics.VEHICLE_RENTED, groupId = "shared-mobility-group")
public void handleVehicleRented(@Payload VehicleRentedEvent event, Acknowledgment ack) {
    if (!idempotencyChecker.processIdempotently(event.getEventId())) {
        ack.acknowledge();
        return;
    }
    vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.IN_USE);
    ack.acknowledge();
}
```

---

### 3. Database Per Service 패턴

**목적**: 각 마이크로서비스가 자체 데이터베이스 스키마를 소유하여 공유 데이터베이스를 통한 강한 결합을 방지합니다.

| 서비스 | 데이터베이스 | 타입 | 스키마 소유 |
|---------|----------|------|-------------|
| User Service | PostgreSQL (5432) | 관계형 | users, roles |
| Vehicle Service | PostgreSQL (5433) | 관계형 | vehicles |
| Rental Service | PostgreSQL (5434) | 관계형 | rentals, rental_sagas |
| Location Service | MongoDB (27017) | 문서형 | location_logs |
| Battery Service | MongoDB (27018) | 문서형 | battery_logs |

---

### 4. Saga 패턴 (오케스트레이션 기반)

**목적**: 보상 액션을 통해 마이크로서비스 간 분산 트랜잭션을 관리합니다.

**Saga 단계**:
1. 차량 예약 (Vehicle Reservation)
2. 결제 처리 (Payment Processing)
3. 차량 잠금 해제 (Vehicle Unlock)
4. 완료 (Completion)

**보상 로직** (실패 시):
```java
private RentalSaga compensateSaga(RentalSaga saga, String reason) {
    saga.setCurrentState(SagaState.COMPENSATING);

    // 역순으로 보상 실행
    for (int i = saga.getSteps().size() - 1; i >= 0; i--) {
        SagaStep step = saga.getSteps().get(i);
        if (step.getSuccess()) {
            compensateStep(saga, step);
        }
    }

    saga.setCurrentState(SagaState.FAILED);
    return sagaRepository.save(saga);
}
```

**파일**: `/services/rental-service/src/main/java/com/next/rentalservice/saga/RentalSagaOrchestrator.java:30`

---

### 5. Hexagonal 아키텍처 (Ports & Adapters)

**목적**: 추상화 계층을 통해 비즈니스 로직을 외부 의존성으로부터 분리합니다.

**포트 (인터페이스)**:
```java
public interface IoTDevicePort {
    int getBatteryLevel(String deviceId);
    Location getLocation(String deviceId);
    void lockVehicle(String deviceId);
    void unlockVehicle(String deviceId);
}
```

**어댑터 구현**:
```java
@Component("mockAdapter")
@Primary
public class MockIoTAdapter implements IoTDevicePort {
    @Override
    public void unlockVehicle(String deviceId) {
        log.info("[MOCK] 차량 잠금 해제: {}", deviceId);
    }
}

@Component("httpAdapter")
public class HttpIoTAdapter implements IoTDevicePort {
    @Override
    public void unlockVehicle(String deviceId) {
        String url = iotApiBaseUrl + "/devices/" + deviceId + "/unlock";
        restTemplate.postForObject(url, null, Void.class);
    }
}
```

**장점**: IoT 제공업체 교체 용이, 실제 IoT 없이 테스트 가능

---

## 생성 패턴

### 1. 빌더 패턴 (Builder Pattern)

**목적**: 읽기 쉬운 구문으로 복잡한 객체를 단계별로 구성합니다.

```java
Rental rental = Rental.builder()
    .userId(userId)
    .vehicleId(vehicleId)
    .status(RentalStatus.ACTIVE)
    .startTime(LocalDateTime.now())
    .startLatitude(latitude)
    .startLongitude(longitude)
    .build();
```

---

### 2. 팩토리 메서드 패턴 (Factory Method Pattern)

**목적**: 의미 있는 이름으로 객체 생성을 위한 정적 팩토리 메서드 제공.

```java
public class ApiResponse<T> {
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }
}

// 사용 예
return ApiResponse.success("대여가 성공적으로 생성되었습니다", rental);
```

---

## 구조 패턴

### 1. 파사드 패턴 (Facade Pattern)

**목적**: 복잡한 하위 시스템에 대한 단순화된 인터페이스 제공.

```java
@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalSagaOrchestrator sagaOrchestrator;

    // 복잡한 대여 플로우 단순화
    public Rental startRental(String userId, String vehicleId, ...) {
        // 1. 대여 엔티티 생성
        Rental rental = Rental.builder()...build();

        // 2. 데이터베이스 저장
        rental = rentalRepository.save(rental);

        // 3. 분산 트랜잭션 Saga 시작
        sagaOrchestrator.startSaga(rental.getId(), vehicleId, userId);

        // 4. 도메인 이벤트 발행
        eventPublisher.publish(KafkaTopics.VEHICLE_RENTED, vehicleId, event);

        return rental;
    }
}
```

---

### 2. 프록시 패턴 (Proxy Pattern)

**목적**: 다른 객체에 대한 대리자 또는 플레이스홀더 제공.

**Redis 캐싱 프록시**:
```java
public Location getLatestLocation(String vehicleId) {
    String cacheKey = "location:" + vehicleId;

    // Redis가 캐싱 프록시 역할
    Object cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return (Location) cached;  // 캐시에서 반환
    }

    // 캐시 미스 → 데이터베이스 쿼리
    Location location = locationRepository.findTopByVehicleId...(vehicleId);

    // 결과 캐싱
    redisTemplate.opsForValue().set(cacheKey, location, 30, TimeUnit.SECONDS);

    return location;
}
```

---

## 행위 패턴

### 1. 옵저버 패턴 (Observer Pattern) - 이벤트 드리븐

**목적**: 상태 변경이 의존 객체들에게 알림을 트리거하는 일대다 의존성 정의.

**발행자 (Subject)**:
```java
// RentalService가 이벤트 발행
eventPublisher.publish(KafkaTopics.VEHICLE_RENTED, vehicleId, event);
eventPublisher.publish(KafkaTopics.VEHICLE_RETURNED, vehicleId, event);
```

**옵저버 (Listener)**:
```java
@Component
public class VehicleEventListener {
    @KafkaListener(topics = KafkaTopics.VEHICLE_RENTED)
    public void handleVehicleRented(VehicleRentedEvent event) {
        // 옵저버 1: 차량 상태 업데이트
    }
}

@Component
public class LocationEventListener {
    @KafkaListener(topics = KafkaTopics.VEHICLE_RENTED)
    public void handleVehicleRented(VehicleRentedEvent event) {
        // 옵저버 2: 시작 위치 기록
    }
}
```

---

### 2. 템플릿 메서드 패턴 (Template Method Pattern)

**목적**: 알고리즘의 골격을 정의하고 하위 클래스가 특정 단계를 재정의하도록 허용.

```java
public abstract class BaseEvent implements DomainEvent {
    protected String eventId;
    protected String eventType;
    protected LocalDateTime timestamp;

    // 템플릿 메서드
    protected BaseEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }
}

// 하위 클래스가 특정 이벤트 구현
public class VehicleRentedEvent extends BaseEvent {
    public VehicleRentedEvent(String vehicleId, ...) {
        super("VehicleRented", vehicleId);  // 템플릿 호출
        this.vehicleId = vehicleId;
        // 특정 필드 설정
    }
}
```

---

## Spring 프레임워크 패턴

### 1. 의존성 주입 (Dependency Injection)

**목적**: 의존성 생성 제어를 클래스에서 컨테이너로 역전.

```java
@Service
@RequiredArgsConstructor  // 생성자 생성 (Lombok)
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalSagaOrchestrator sagaOrchestrator;

    // Spring이 생성자를 통해 자동으로 의존성 주입
}
```

---

### 2. 리포지토리 패턴 (Repository Pattern)

**목적**: 리포지토리 인터페이스 뒤에 데이터 액세스 로직 캡슐화.

```java
public interface RentalRepository extends JpaRepository<Rental, String> {
    List<Rental> findByUserId(String userId);
    List<Rental> findByVehicleId(String vehicleId);
    Optional<Rental> findTopByUserIdAndStatusOrderByStartTimeDesc(
        String userId, RentalStatus status
    );
}

// Spring이 자동으로 이 메서드들을 구현!
// 메서드 이름으로부터 쿼리 파생
```

---

## 엔터프라이즈 통합 패턴

### 1. 멱등성 소비자 (Idempotent Consumer)

**목적**: at-least-once 전달에서도 메시지 처리가 정확히 한 번만 발생하도록 보장.

```java
@Component
@RequiredArgsConstructor
public class IdempotencyChecker {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean processIdempotently(String eventId) {
        String key = "processed:" + eventId;

        if (isProcessed(key)) {
            log.info("중복 이벤트 건너뛰기: {}", eventId);
            return false;
        }

        return markAsProcessed(key);
    }

    private boolean markAsProcessed(String key) {
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, "processed", 7, TimeUnit.DAYS);
        return Boolean.TRUE.equals(success);
    }
}
```

**사용법**:
```java
@KafkaListener(topics = "vehicle.rented")
public void handleVehicleRented(VehicleRentedEvent event, Acknowledgment ack) {
    if (!idempotencyChecker.processIdempotently(event.getEventId())) {
        ack.acknowledge();  // 중복 건너뛰기
        return;
    }

    // 이벤트 처리
    vehicleService.updateVehicleStatus(...);
    ack.acknowledge();
}
```

**장점**:
- Kafka의 at-least-once 전달 안전
- Redis `SETNX`가 원자적 체크-앤-셋 제공
- 7일 TTL로 메모리와 재생 윈도우 균형

---

### 2. 이벤트 소싱 (Event Sourcing) - 부분 구현

**목적**: 상태 변경을 이벤트 시퀀스로 저장.

```java
@Document(collection = "location_logs")
public class Location {
    @Id
    private String id;

    @Indexed
    private String vehicleId;

    @Indexed
    private LocalDateTime timestamp;

    // 불변: 업데이트하지 않고 새 레코드만 삽입
}

// 과거 위치 쿼리 (이벤트 소싱의 장점)
List<Location> trail = locationRepository
    .findByVehicleIdAndTimestampBetween(
        vehicleId,
        startTime,
        endTime
    );
```

**장점**:
- 차량 이동의 전체 이력 (감사 추적)
- 시간 여행 쿼리 (어제 오후 2시에 차량이 어디 있었나?)
- 분석 (인기 경로, 속도 패턴 등)

---

## 동시성 패턴

### 1. 낙관적 잠금 (Optimistic Locking)

**목적**: 비관적 잠금 없이 동시 시나리오에서 손실된 업데이트 방지.

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    private String id;

    @Version  // 낙관적 잠금
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**동작 방식**:
```
사용자 A가 Vehicle 읽기 (version=1)
사용자 B가 Vehicle 읽기 (version=1)

사용자 A가 Vehicle 업데이트 (version=1 → 2)  ✅ 성공

사용자 B가 Vehicle 업데이트 시도 (version=1)  ❌ 예외 발생
  → 버전 불일치! 1을 예상했지만 DB에는 2가 있음
  → 최신 데이터로 재시도
```

**장점**:
- 데이터베이스 잠금 없음 (더 나은 동시성)
- 손실된 업데이트 방지 (마지막 쓰기가 첫 번째를 덮어쓰지 않음)
- JPA가 자동으로 version 필드 관리

---

## 패턴 요약 표

| 패턴 | 카테고리 | 복잡도 | 사용 위치 |
|------|---------|-------|---------|
| Microservices | 아키텍처 | 높음 | 모든 서비스 |
| Event-Driven | 아키텍처 | 높음 | Kafka 통합 |
| Database Per Service | 아키텍처 | 중간 | 모든 서비스 |
| Saga | 아키텍처 | 높음 | RentalService |
| Hexagonal (Ports & Adapters) | 아키텍처 | 중간 | VehicleService (IoT) |
| Builder | 생성 | 낮음 | 모든 엔티티 |
| Factory Method | 생성 | 낮음 | ApiResponse, Location |
| Facade | 구조 | 중간 | 서비스 계층 |
| Proxy | 구조 | 중간 | @Transactional, Redis 캐시 |
| Adapter | 구조 | 중간 | IoT 어댑터 |
| Observer | 행위 | 높음 | Kafka 이벤트 |
| Template Method | 행위 | 낮음 | BaseEvent |
| Dependency Injection | Spring | 중간 | 모든 서비스 |
| Repository | Spring | 낮음 | 모든 데이터 액세스 |
| Idempotent Consumer | 통합 | 중간 | 이벤트 리스너 |
| Event Sourcing | 통합 | 높음 | Location, BatteryLog |
| Optimistic Locking | 동시성 | 낮음 | 모든 엔티티 |

---

## 패턴 선택 가이드

### 각 패턴을 사용해야 하는 경우

**마이크로서비스**: 서로 다른 컴포넌트가 다음을 가질 때:
- 서로 다른 확장 요구사항
- 서로 다른 기술 요구
- 독립적인 배포 주기

**이벤트 드리븐**: 다음이 필요할 때:
- 서비스 간 느슨한 결합
- 비동기 처리
- 이벤트 감사 추적

**Saga**: 다음이 필요할 때:
- 2PC 없는 분산 트랜잭션
- 명확한 롤백 의미론
- 마이크로서비스 아키텍처

**리포지토리**: 다음을 원할 때:
- 데이터 액세스에 대한 추상화
- 테스트 가능성 (리포지토리 모킹)
- 쿼리 메서드 자동 구현

**멱등성 소비자**: 다음이 있을 때:
- at-least-once 메시지 전달
- 중복 처리 위험
- 정확히 한 번 의미론 필요

---

## 피한 안티패턴

❌ **분산 모놀리스**: 각 서비스가 자체 데이터베이스를 가짐 (공유하지 않음)

❌ **수다쟁이 서비스**: 서비스 간 동기 REST 호출 대신 이벤트 사용

❌ **강한 결합**: 서비스가 Kafka를 통해 통신 (직접 API 호출 아님)

❌ **빈약한 도메인 모델**: 엔티티가 행위를 가짐 (`rental.complete()`, `vehicle.isAvailable()`)

❌ **신 클래스**: 서비스가 단일 책임 (RentalService는 대여만 처리)

❌ **매직 넘버**: `application.yml`에 설정 (하드코딩 안 함)

---

## 참고 문헌

- **마이크로서비스 패턴**: Chris Richardson
- **엔터프라이즈 통합 패턴**: Gregor Hohpe, Bobby Woolf
- **디자인 패턴**: Gang of Four (GoF)
- **도메인 주도 설계**: Eric Evans
- **Spring Framework 문서**: https://spring.io/projects/spring-framework

---

**문서 버전**: 1.0 (한글)
**마지막 업데이트**: 2025-01-24
**관리자**: 개발팀
