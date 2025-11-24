# Design Patterns in Shared Mobility Platform

This document catalogs all design patterns used in the project, organized by category. Each pattern includes:
- **Intent**: Why the pattern is used
- **Implementation**: Where and how it's implemented
- **Benefits**: Advantages gained
- **Code Examples**: Actual code from the project

---

## Table of Contents

1. [Architectural Patterns](#architectural-patterns)
2. [Creational Patterns](#creational-patterns)
3. [Structural Patterns](#structural-patterns)
4. [Behavioral Patterns](#behavioral-patterns)
5. [Spring Framework Patterns](#spring-framework-patterns)
6. [Enterprise Integration Patterns](#enterprise-integration-patterns)
7. [Concurrency Patterns](#concurrency-patterns)

---

## Architectural Patterns

### 1. Microservices Architecture

**Intent**: Decompose application into independently deployable services, each owning its data and business logic.

**Implementation**:
```
services/
├── user-service/       (Port 8081, PostgreSQL)
├── vehicle-service/    (Port 8082, PostgreSQL)
├── rental-service/     (Port 8083, PostgreSQL)
├── location-service/   (Port 8084, MongoDB)
└── battery-service/    (Port 8085, MongoDB)
```

**Benefits**:
- Independent scaling (scale Location Service to 20 instances, others to 5)
- Technology diversity (JPA for transactional, MongoDB for time-series)
- Fault isolation (if Battery Service crashes, rentals still work)
- Team autonomy (separate teams can own separate services)

**Code Evidence**:
```java
// Each service is a separate Spring Boot application
@SpringBootApplication
public class RentalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalServiceApplication.class, args);
    }
}
```

**File**: `/services/rental-service/src/main/java/com/next/rentalservice/RentalServiceApplication.java`

---

### 2. Event-Driven Architecture (EDA)

**Intent**: Enable loose coupling between services through asynchronous event propagation.

**Implementation**: Kafka as central event bus with 14 domain event topics.

**Kafka Topics**:
- `vehicle.rented`, `vehicle.returned`, `vehicle.moved`, `vehicle.status.changed`
- `battery.low`, `battery.critical`, `battery.updated`
- `location.updated`
- `rental.started`, `rental.completed`, `rental.cancelled`
- `payment.completed`, `payment.failed`
- `maintenance.required`, `maintenance.completed`

**Benefits**:
- Temporal decoupling (producer doesn't wait for consumer)
- Service independence (new consumer can subscribe without changing producer)
- Event replay (Kafka retains events for 7 days)
- Audit trail (all events logged immutably)

**Code Example** (Publisher):
```java
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Override
    public void publish(String topic, String key, DomainEvent event) {
        log.info("Publishing event: {} to topic: {}", event.getEventType(), topic);
        kafkaTemplate.send(topic, key, event);
    }
}
```

**File**: `/common/common-event/src/main/java/com/next/common/event/publisher/KafkaEventPublisher.java`

**Code Example** (Consumer):
```java
@Component
@RequiredArgsConstructor
public class VehicleEventListener {
    private final VehicleService vehicleService;
    private final IdempotencyChecker idempotencyChecker;

    @KafkaListener(
        topics = KafkaTopics.VEHICLE_RENTED,
        groupId = "shared-mobility-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleRented(
        @Payload VehicleRentedEvent event,
        Acknowledgment acknowledgment
    ) {
        if (!idempotencyChecker.processIdempotently(event.getEventId())) {
            acknowledgment.acknowledge();
            return;
        }

        vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.IN_USE);
        acknowledgment.acknowledge();
    }
}
```

**File**: `/services/vehicle-service/src/main/java/com/next/vehicleservice/consumer/VehicleEventListener.java:28`

---

### 3. Database Per Service Pattern

**Intent**: Each microservice owns its database schema, preventing tight coupling through shared databases.

**Implementation**:

| Service | Database | Type | Schema Ownership |
|---------|----------|------|------------------|
| User Service | PostgreSQL (port 5432) | Relational | users, roles |
| Vehicle Service | PostgreSQL (port 5433) | Relational | vehicles |
| Rental Service | PostgreSQL (port 5434) | Relational | rentals, rental_sagas |
| Location Service | MongoDB (port 27017) | Document | location_logs |
| Battery Service | MongoDB (port 27018) | Document | battery_logs |

**Benefits**:
- No cross-service database queries (enforces service boundaries)
- Independent database technology choices (PostgreSQL vs MongoDB)
- Independent schema evolution (no breaking changes across services)
- Scalability (each database can scale independently)

**Code Example**:
```java
// User Service - JPA Repository (PostgreSQL)
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}

// Location Service - MongoDB Repository
public interface LocationRepository extends MongoRepository<Location, String> {
    List<Location> findByVehicleIdOrderByTimestampDesc(String vehicleId, Pageable pageable);
}
```

**Files**:
- `/services/user-service/src/main/java/com/next/userservice/repository/UserRepository.java`
- `/services/location-service/src/main/java/com/next/locationservice/repository/LocationRepository.java`

---

### 4. Saga Pattern (Orchestration-Based)

**Intent**: Manage distributed transactions across microservices with compensating actions for failures.

**Implementation**: `RentalSagaOrchestrator` coordinates rental transaction across multiple services.

**Saga Steps**:
1. **Vehicle Reservation**: Reserve vehicle for rental
2. **Payment Processing**: Authorize payment
3. **Vehicle Unlock**: Send IoT unlock command
4. **Completion**: Mark saga as completed

**Compensation Logic** (if failure occurs):
- Step 3 fails → Refund payment, release vehicle reservation
- Step 2 fails → Release vehicle reservation
- Step 1 fails → No compensation needed

**Benefits**:
- Distributed transaction coordination without 2PC
- Eventual consistency acceptable for rental domain
- Clear rollback semantics with compensation
- State persistence in Redis for fault tolerance

**Code Example**:
```java
@Service
@RequiredArgsConstructor
public class RentalSagaOrchestrator {
    private final RentalSagaRepository sagaRepository;

    public RentalSaga startSaga(String rentalId, String vehicleId, String userId) {
        RentalSaga saga = RentalSaga.builder()
            .sagaId(UUID.randomUUID().toString())
            .rentalId(rentalId)
            .vehicleId(vehicleId)
            .userId(userId)
            .currentState(SagaState.STARTED)
            .startedAt(LocalDateTime.now())
            .steps(new ArrayList<>())
            .retryCount(0)
            .build();

        saga = sagaRepository.save(saga);

        try {
            saga = executeVehicleReservation(saga);
            saga = executePaymentProcessing(saga);
            saga = executeVehicleUnlock(saga);
            return completeSaga(saga);
        } catch (Exception e) {
            return compensateSaga(saga, e.getMessage());
        }
    }

    private RentalSaga executeVehicleReservation(RentalSaga saga) {
        // Reserve vehicle logic
        saga.setCurrentState(SagaState.VEHICLE_RESERVED);
        saga.getSteps().add(createStep("VEHICLE_RESERVATION", true));
        return sagaRepository.save(saga);
    }

    private RentalSaga compensateSaga(RentalSaga saga, String reason) {
        saga.setCurrentState(SagaState.COMPENSATING);
        saga.setFailureReason(reason);

        // Execute compensations in reverse order
        for (int i = saga.getSteps().size() - 1; i >= 0; i--) {
            SagaStep step = saga.getSteps().get(i);
            if (step.getSuccess()) {
                compensateStep(saga, step);
            }
        }

        saga.setCurrentState(SagaState.FAILED);
        saga.setCompletedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }

    private void compensateStep(RentalSaga saga, SagaStep step) {
        String action = getCompensationAction(step.getStepName());
        log.info("Compensating step: {} with action: {}", step.getStepName(), action);
        // Execute compensation logic
    }

    private String getCompensationAction(String stepName) {
        return switch (stepName) {
            case "VEHICLE_RESERVATION" -> "Release vehicle reservation";
            case "PAYMENT_PROCESSING" -> "Refund payment";
            case "VEHICLE_UNLOCK" -> "Lock vehicle";
            default -> "No compensation action defined";
        };
    }
}
```

**File**: `/services/rental-service/src/main/java/com/next/rentalservice/saga/RentalSagaOrchestrator.java:30`

---

### 5. Hexagonal Architecture (Ports & Adapters)

**Intent**: Decouple business logic from external dependencies through abstraction layers.

**Implementation**: IoT device integration uses port-adapter pattern.

**Port (Interface)**:
```java
public interface IoTDevicePort {
    int getBatteryLevel(String deviceId);
    Location getLocation(String deviceId);
    void lockVehicle(String deviceId);
    void unlockVehicle(String deviceId);
}
```

**File**: `/services/vehicle-service/src/main/java/com/next/vehicleservice/port/IoTDevicePort.java`

**Adapters (Implementations)**:

**Mock Adapter** (for development/testing):
```java
@Component("mockAdapter")
@Primary
public class MockIoTAdapter implements IoTDevicePort {
    private final Map<String, Integer> batteryLevels = new ConcurrentHashMap<>();
    private final Map<String, Location> locations = new ConcurrentHashMap<>();

    @Override
    public int getBatteryLevel(String deviceId) {
        return batteryLevels.getOrDefault(deviceId, 100);
    }

    @Override
    public void unlockVehicle(String deviceId) {
        log.info("[MOCK] Unlocking vehicle with device ID: {}", deviceId);
        // Simulate IoT command
    }
}
```

**File**: `/services/vehicle-service/src/main/java/com/next/vehicleservice/adapter/MockIoTAdapter.java`

**HTTP Adapter** (for production):
```java
@Component("httpAdapter")
public class HttpIoTAdapter implements IoTDevicePort {
    private final RestTemplate restTemplate;
    private final String iotApiBaseUrl;

    @Override
    public int getBatteryLevel(String deviceId) {
        String url = iotApiBaseUrl + "/devices/" + deviceId + "/battery";
        return restTemplate.getForObject(url, Integer.class);
    }

    @Override
    public void unlockVehicle(String deviceId) {
        String url = iotApiBaseUrl + "/devices/" + deviceId + "/unlock";
        restTemplate.postForObject(url, null, Void.class);
    }
}
```

**File**: `/services/vehicle-service/src/main/java/com/next/vehicleservice/adapter/HttpIoTAdapter.java`

**Usage in Service**:
```java
@Service
@RequiredArgsConstructor
public class VehicleService {
    @Qualifier("mockAdapter")  // or "httpAdapter" for production
    private final IoTDevicePort iotDevicePort;

    public void unlockVehicle(String vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        iotDevicePort.unlockVehicle(vehicle.getIotDeviceId());
    }
}
```

**Benefits**:
- Easy to swap IoT providers (just change `@Qualifier`)
- Testable without real IoT devices (use MockAdapter)
- Follows Dependency Inversion Principle (depend on abstraction)

---

## Creational Patterns

### 1. Builder Pattern

**Intent**: Construct complex objects step-by-step with readable syntax.

**Implementation**: Lombok `@Builder` annotation on all domain entities and DTOs.

**Code Example**:
```java
@Entity
@Table(name = "rentals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental extends BaseEntity {
    private String userId;
    private String vehicleId;
    private RentalStatus status;
    private LocalDateTime startTime;
    // ... other fields
}

// Usage
Rental rental = Rental.builder()
    .userId(userId)
    .vehicleId(vehicleId)
    .status(RentalStatus.ACTIVE)
    .startTime(LocalDateTime.now())
    .startLatitude(latitude)
    .startLongitude(longitude)
    .startBatteryLevel(batteryLevel)
    .build();
```

**File**: `/common/common-domain/src/main/java/com/next/common/domain/model/Rental.java`

**Benefits**:
- Immutable objects (no setters needed after construction)
- Named parameters (clear what each value represents)
- Optional fields (only set what you need)
- Compile-time safety (all required fields enforced)

---

### 2. Factory Method Pattern

**Intent**: Provide static factory methods for object creation with meaningful names.

**Implementation**: Static factory methods in entities and DTOs.

**Code Example**:
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = success(data);
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> ApiResponse<T> error(String message, ErrorDetails errorDetails) {
        ApiResponse<T> response = error(message);
        response.error = errorDetails;
        return response;
    }
}

// Usage
return ApiResponse.success("Rental created successfully", rental);
return ApiResponse.error("Vehicle not available");
```

**File**: `/common/common-domain/src/main/java/com/next/common/domain/dto/ApiResponse.java`

**More Examples**:
```java
// Location
public static Location of(String vehicleId, Double latitude, Double longitude) {
    Location location = new Location();
    location.setVehicleId(vehicleId);
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    location.setTimestamp(LocalDateTime.now());
    location.updateCoordinates();
    return location;
}

// BatteryLog
public static BatteryLog of(String vehicleId, Integer batteryLevel) {
    return BatteryLog.builder()
        .vehicleId(vehicleId)
        .batteryLevel(batteryLevel)
        .timestamp(LocalDateTime.now())
        .source("SYSTEM")
        .build();
}
```

**Benefits**:
- Descriptive method names (`success()`, `error()`, `of()`)
- Encapsulates complex initialization logic
- Can return cached instances (not used here, but possible)

---

### 3. Singleton Pattern (Implicit via Spring)

**Intent**: Ensure only one instance of a class exists.

**Implementation**: Spring beans are singletons by default.

**Code Example**:
```java
@Service
@RequiredArgsConstructor
public class RentalService {
    // Spring creates ONE instance of RentalService
    // All @Autowired references get the same instance
}

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    // ONE instance shared across all services
}
```

**Benefits**:
- Memory efficiency (one instance for stateless services)
- Thread-safe (Spring manages bean lifecycle)
- Configuration in one place (`@Bean` methods)

---

## Structural Patterns

### 1. Facade Pattern

**Intent**: Provide simplified interface to complex subsystems.

**Implementation**: Service layer acts as facade to repositories, event publishers, and orchestrators.

**Code Example**:
```java
@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalSagaOrchestrator sagaOrchestrator;

    // Facade simplifies complex rental flow
    public Rental startRental(String userId, String vehicleId,
                              Double latitude, Double longitude,
                              Integer batteryLevel) {
        // 1. Create rental entity
        Rental rental = Rental.builder()
            .userId(userId)
            .vehicleId(vehicleId)
            .status(RentalStatus.ACTIVE)
            .startTime(LocalDateTime.now())
            .startLatitude(latitude)
            .startLongitude(longitude)
            .startBatteryLevel(batteryLevel)
            .build();

        // 2. Save to database
        rental = rentalRepository.save(rental);

        // 3. Start distributed transaction saga
        sagaOrchestrator.startSaga(rental.getId(), vehicleId, userId);

        // 4. Publish domain event
        VehicleRentedEvent event = new VehicleRentedEvent(
            vehicleId, userId, rental.getId(),
            latitude, longitude, batteryLevel
        );
        eventPublisher.publish(KafkaTopics.VEHICLE_RENTED, vehicleId, event);

        return rental;
    }
}
```

**File**: `/services/rental-service/src/main/java/com/next/rentalservice/service/RentalService.java:45`

**Benefits**:
- Controller doesn't need to know about Saga, Kafka, etc.
- Business logic centralized in service layer
- Easy to test (mock dependencies)

---

### 2. Proxy Pattern (Implicit)

**Intent**: Provide surrogate or placeholder for another object.

**Implementation**: Spring AOP creates proxies for `@Transactional`, JPA lazy loading, Redis caching.

**Code Example** (Transactional Proxy):
```java
@Transactional
public Rental startRental(...) {
    // Spring creates proxy that:
    // 1. Begins transaction
    // 2. Executes this method
    // 3. Commits transaction (or rolls back on exception)
}
```

**Code Example** (Redis Caching Proxy):
```java
public Location getLatestLocation(String vehicleId) {
    String cacheKey = "location:" + vehicleId;

    // Redis acts as caching proxy
    Object cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return (Location) cached;  // Return from cache
    }

    // Cache miss → query database
    Location location = locationRepository
        .findTopByVehicleIdOrderByTimestampDesc(vehicleId);

    // Cache result
    redisTemplate.opsForValue().set(cacheKey, location, 30, TimeUnit.SECONDS);

    return location;
}
```

**File**: `/services/location-service/src/main/java/com/next/locationservice/service/LocationService.java:55`

**Benefits**:
- Lazy loading for JPA entities
- Transparent caching (business logic doesn't know about Redis)
- Transaction management without boilerplate

---

### 3. Adapter Pattern

**Intent**: Convert interface of a class into another interface expected by clients.

**See**: [Hexagonal Architecture (Ports & Adapters)](#5-hexagonal-architecture-ports--adapters) above

---

## Behavioral Patterns

### 1. Observer Pattern (Event-Driven)

**Intent**: Define one-to-many dependency where state changes trigger notifications to dependents.

**Implementation**: Kafka publish-subscribe with event listeners.

**Subjects (Publishers)**:
```java
// RentalService publishes events
eventPublisher.publish(KafkaTopics.VEHICLE_RENTED, vehicleId, event);
eventPublisher.publish(KafkaTopics.VEHICLE_RETURNED, vehicleId, event);

// BatteryService publishes alerts
eventPublisher.publish(KafkaTopics.BATTERY_LOW, vehicleId, batteryLowEvent);
```

**Observers (Listeners)**:
```java
@Component
public class VehicleEventListener {
    @KafkaListener(topics = KafkaTopics.VEHICLE_RENTED)
    public void handleVehicleRented(VehicleRentedEvent event) {
        // Observer 1: Update vehicle status
    }

    @KafkaListener(topics = KafkaTopics.VEHICLE_RETURNED)
    public void handleVehicleReturned(VehicleReturnedEvent event) {
        // Observer 1: Update vehicle status
    }
}

@Component
public class LocationEventListener {
    @KafkaListener(topics = KafkaTopics.VEHICLE_RENTED)
    public void handleVehicleRented(VehicleRentedEvent event) {
        // Observer 2: Record start location
    }
}
```

**Benefits**:
- Loose coupling (publisher doesn't know about observers)
- New observers can subscribe without changing publisher
- Observers run in parallel (Kafka consumer groups)

---

### 2. Template Method Pattern

**Intent**: Define skeleton of algorithm, letting subclasses override specific steps.

**Implementation**: `BaseEvent` provides template for event creation.

**Code Example**:
```java
@Getter
@Setter
public abstract class BaseEvent implements DomainEvent {
    protected String eventId;
    protected String eventType;
    protected LocalDateTime timestamp;
    protected String aggregateId;
    protected String version;

    // Template method
    protected BaseEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.version = "1.0";
    }
}

// Subclasses implement specific events
public class VehicleRentedEvent extends BaseEvent {
    private String vehicleId;
    private String userId;
    private String rentalId;
    // ... specific fields

    public VehicleRentedEvent(String vehicleId, String userId, ...) {
        super("VehicleRented", vehicleId);  // Call template
        this.vehicleId = vehicleId;
        this.userId = userId;
        // ... set specific fields
    }
}
```

**File**: `/common/common-event/src/main/java/com/next/common/event/model/BaseEvent.java`

**Benefits**:
- Common event metadata (eventId, timestamp) handled once
- Subclasses focus on domain-specific fields
- Consistent event structure across system

---

### 3. Chain of Responsibility Pattern

**Intent**: Pass request along chain of handlers until one processes it.

**Implementation**: Saga step execution with compensation chain.

**Code Example**:
```java
// Forward chain (execute steps sequentially)
saga = executeVehicleReservation(saga);
saga = executePaymentProcessing(saga);
saga = executeVehicleUnlock(saga);

// If failure, reverse chain (compensation)
if (stepFails) {
    for (int i = saga.getSteps().size() - 1; i >= 0; i--) {
        SagaStep step = saga.getSteps().get(i);
        if (step.getSuccess()) {
            compensateStep(saga, step);  // Reverse operation
        }
    }
}
```

**Benefits**:
- Each step can decide to continue or stop the chain
- Easy to add new steps or reorder
- Clear compensation logic (reverse chain)

---

### 4. Strategy Pattern

**Intent**: Define family of algorithms, make them interchangeable.

**Implementation**: IoT adapter selection (can inject different strategies).

**Code Example**:
```java
public class VehicleService {
    @Qualifier("mockAdapter")  // Strategy 1: Mock
    // @Qualifier("httpAdapter")  // Strategy 2: HTTP
    // @Qualifier("mqttAdapter")  // Strategy 3: MQTT (future)
    private final IoTDevicePort iotStrategy;

    public void unlockVehicle(String vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        iotStrategy.unlockVehicle(vehicle.getIotDeviceId());
        // Algorithm (mock vs HTTP vs MQTT) is encapsulated in strategy
    }
}
```

**Benefits**:
- Swap algorithms at runtime (or via configuration)
- Each strategy testable independently
- Open/Closed Principle (add new strategies without changing VehicleService)

---

## Spring Framework Patterns

### 1. Dependency Injection (DI)

**Intent**: Invert control of dependency creation from classes to container.

**Implementation**: Constructor injection with `@RequiredArgsConstructor` (Lombok).

**Code Example**:
```java
@Service
@RequiredArgsConstructor  // Generates constructor with final fields
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalSagaOrchestrator sagaOrchestrator;

    // Spring automatically injects dependencies via constructor
}

// Equivalent to (without Lombok):
@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalSagaOrchestrator sagaOrchestrator;

    @Autowired
    public RentalService(RentalRepository rentalRepository,
                         EventPublisher eventPublisher,
                         RentalSagaOrchestrator sagaOrchestrator) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
        this.sagaOrchestrator = sagaOrchestrator;
    }
}
```

**Benefits**:
- Testability (inject mocks in tests)
- Loose coupling (depend on interfaces, not concrete classes)
- Configuration externalized (Spring manages bean wiring)

---

### 2. Repository Pattern

**Intent**: Encapsulate data access logic behind repository interfaces.

**Implementation**: Spring Data JPA and MongoDB repositories.

**Code Example**:
```java
public interface RentalRepository extends JpaRepository<Rental, String> {
    List<Rental> findByUserId(String userId);
    List<Rental> findByVehicleId(String vehicleId);
    Optional<Rental> findTopByUserIdAndStatusOrderByStartTimeDesc(
        String userId, RentalStatus status
    );
}

// Spring automatically implements these methods!
// Query derived from method name
```

**File**: `/services/rental-service/src/main/java/com/next/rentalservice/repository/RentalRepository.java`

**Benefits**:
- Abstraction over data access (SQL, MongoDB, Redis)
- Automatic implementation (no boilerplate code)
- Query method naming convention (findByXxxAndYyy)
- Supports pagination, sorting, custom queries

---

### 3. Service Layer Pattern

**Intent**: Define application's boundary and business operations.

**Implementation**: `@Service` classes orchestrate business logic.

**Layered Architecture**:
```
Controller (REST API)
    ↓ (calls)
Service (Business Logic)
    ↓ (calls)
Repository (Data Access)
```

**Code Example**:
```java
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping("/start")
    public ApiResponse<Rental> startRental(@RequestBody StartRentalRequest request) {
        Rental rental = rentalService.startRental(...);
        return ApiResponse.success("Rental created successfully", rental);
    }
}

@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    // Business logic here
}
```

**Benefits**:
- Separation of concerns (controller = HTTP, service = business, repository = data)
- Reusable business logic (service can be called from multiple controllers)
- Transactional boundaries (`@Transactional` on service methods)

---

## Enterprise Integration Patterns

### 1. Idempotent Consumer

**Intent**: Ensure message processing happens exactly once, even with at-least-once delivery.

**Implementation**: `IdempotencyChecker` with Redis.

**Code Example**:
```java
@Component
@RequiredArgsConstructor
public class IdempotencyChecker {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean processIdempotently(String eventId) {
        String key = "processed:" + eventId;

        if (isProcessed(key)) {
            log.info("Skipping duplicate event: {}", eventId);
            return false;
        }

        return markAsProcessed(key);
    }

    private boolean isProcessed(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private boolean markAsProcessed(String key) {
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, "processed", 7, TimeUnit.DAYS);
        return Boolean.TRUE.equals(success);
    }
}
```

**File**: `/common/common-event/src/main/java/com/next/common/event/consumer/IdempotencyChecker.java:22`

**Usage**:
```java
@KafkaListener(topics = "vehicle.rented")
public void handleVehicleRented(VehicleRentedEvent event, Acknowledgment ack) {
    if (!idempotencyChecker.processIdempotently(event.getEventId())) {
        ack.acknowledge();  // Skip duplicate
        return;
    }

    // Process event
    vehicleService.updateVehicleStatus(...);
    ack.acknowledge();
}
```

**Benefits**:
- Kafka at-least-once delivery safe
- Redis `SETNX` provides atomic check-and-set
- 7-day TTL balances memory vs. replay window

---

### 2. Event Sourcing (Partial)

**Intent**: Store state changes as sequence of events.

**Implementation**: Location and BatteryLog as append-only event logs.

**Code Example**:
```java
@Document(collection = "location_logs")
@Getter
@Setter
public class Location {
    @Id
    private String id;

    @Indexed
    private String vehicleId;

    @Indexed
    private LocalDateTime timestamp;

    // Immutable: never update, only insert new records
}

// Query historical locations (event sourcing benefit)
List<Location> trail = locationRepository
    .findByVehicleIdAndTimestampBetween(
        vehicleId,
        startTime,
        endTime
    );
```

**File**: `/common/common-domain/src/main/java/com/next/common/domain/model/Location.java`

**Benefits**:
- Full history of vehicle movements (audit trail)
- Time-travel queries (where was vehicle at 2pm yesterday?)
- Analytics (popular routes, speed patterns, etc.)

---

### 3. Compensating Transaction (Saga)

**See**: [Saga Pattern](#4-saga-pattern-orchestration-based) above

---

### 4. Message Channel (Kafka Topics)

**Intent**: Use separate channels for different message types.

**Implementation**: 14 Kafka topics for different event types.

**Code Example**:
```java
public class KafkaTopics {
    // Vehicle events
    public static final String VEHICLE_RENTED = "vehicle.rented";
    public static final String VEHICLE_RETURNED = "vehicle.returned";
    public static final String VEHICLE_MOVED = "vehicle.moved";

    // Battery events
    public static final String BATTERY_LOW = "battery.low";
    public static final String BATTERY_CRITICAL = "battery.critical";

    // Rental events
    public static final String RENTAL_STARTED = "rental.started";
    public static final String RENTAL_COMPLETED = "rental.completed";
}
```

**File**: `/common/common-event/src/main/java/com/next/common/event/config/KafkaTopics.java`

**Benefits**:
- Consumers subscribe only to relevant events
- Easy to add new event types (new topic)
- Partitioning by vehicleId for ordered processing

---

## Concurrency Patterns

### 1. Thread Pool (Kafka Consumer Concurrency)

**Intent**: Limit concurrent event processing to avoid overwhelming system.

**Implementation**: Kafka listener concurrency factor.

**Code Example**:
```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3);  // 3 threads per consumer
    factory.getContainerProperties().setAckMode(AckMode.MANUAL);
    return factory;
}
```

**File**: `/common/common-event/src/main/java/com/next/common/event/config/KafkaConsumerConfig.java`

**Benefits**:
- Process 3 events in parallel per consumer instance
- Avoid overwhelming database with too many concurrent writes
- Configurable based on system resources

---

### 2. Optimistic Locking

**Intent**: Prevent lost updates in concurrent scenarios without pessimistic locks.

**Implementation**: `@Version` field in `BaseEntity`.

**Code Example**:
```java
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    private String id;

    @Version  // Optimistic locking
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**File**: `/common/common-domain/src/main/java/com/next/common/domain/model/BaseEntity.java`

**How It Works**:
```
User A reads Vehicle (version=1)
User B reads Vehicle (version=1)

User A updates Vehicle (version=1 → 2)  ✅ Success

User B tries to update Vehicle (version=1)  ❌ Exception
  → Version mismatch! Expected 1, but DB has 2
  → Retry with latest data
```

**Benefits**:
- No database locks (better concurrency)
- Prevents lost updates (last write doesn't overwrite first)
- JPA automatically manages version field

---

## Summary Table

| Pattern | Category | Complexity | Used In |
|---------|----------|------------|---------|
| Microservices | Architectural | High | All services |
| Event-Driven Architecture | Architectural | High | Kafka integration |
| Database Per Service | Architectural | Medium | All services |
| Saga | Architectural | High | RentalService |
| Hexagonal (Ports & Adapters) | Architectural | Medium | VehicleService (IoT) |
| Builder | Creational | Low | All entities |
| Factory Method | Creational | Low | ApiResponse, Location |
| Singleton | Creational | Low | Spring beans |
| Facade | Structural | Medium | Service layer |
| Proxy | Structural | Medium | @Transactional, Redis cache |
| Adapter | Structural | Medium | IoT adapters |
| Observer | Behavioral | High | Kafka events |
| Template Method | Behavioral | Low | BaseEvent |
| Chain of Responsibility | Behavioral | Medium | Saga compensation |
| Strategy | Behavioral | Medium | IoT adapter selection |
| Dependency Injection | Spring | Medium | All services |
| Repository | Spring | Low | All data access |
| Service Layer | Spring | Low | All business logic |
| Idempotent Consumer | Integration | Medium | Event listeners |
| Event Sourcing | Integration | High | Location, BatteryLog |
| Compensating Transaction | Integration | High | Saga |
| Thread Pool | Concurrency | Low | Kafka consumers |
| Optimistic Locking | Concurrency | Low | All entities |

---

## Pattern Selection Guidelines

### When to Use Each Pattern

**Microservices**: When different components have:
- Different scaling requirements
- Different technology needs
- Independent deployment cycles

**Event-Driven**: When you need:
- Loose coupling between services
- Asynchronous processing
- Event audit trail

**Saga**: When you need:
- Distributed transactions without 2PC
- Clear rollback semantics
- Microservices architecture

**Repository**: When you want:
- Abstraction over data access
- Testability (mock repositories)
- Query method auto-implementation

**Idempotent Consumer**: When you have:
- At-least-once message delivery
- Risk of duplicate processing
- Need for exactly-once semantics

---

## Anti-Patterns Avoided

**❌ Distributed Monolith**: Each service has its own database (not shared)

**❌ Chatty Services**: Use events instead of synchronous REST calls between services

**❌ Tight Coupling**: Services communicate via Kafka (not direct API calls)

**❌ Anemic Domain Model**: Entities have behavior (`rental.complete()`, `vehicle.isAvailable()`)

**❌ God Class**: Services have single responsibility (RentalService only handles rentals)

**❌ Magic Numbers**: Configuration in `application.yml` (not hardcoded)

---

## References

- **Microservices Patterns**: Chris Richardson
- **Enterprise Integration Patterns**: Gregor Hohpe, Bobby Woolf
- **Design Patterns**: Gang of Four (GoF)
- **Domain-Driven Design**: Eric Evans
- **Spring Framework Documentation**: https://spring.io/projects/spring-framework

---

**Document Version**: 1.0
**Last Updated**: 2025-01-24
**Maintainer**: Development Team
