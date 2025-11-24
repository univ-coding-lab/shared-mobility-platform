# Architecture Overview: Shared Mobility Platform

This document provides a comprehensive overview of the system architecture using C4 model diagrams.

## System Context Diagram

```mermaid
C4Context
    title System Context - Shared Mobility Platform

    Person(user, "User", "Mobile app user renting vehicles")
    Person(operator, "Operations Team", "Manages fleet, charging, maintenance")
    Person(admin, "Administrator", "System configuration, user management")

    System(smp, "Shared Mobility Platform", "Event-driven microservices platform for vehicle sharing")

    System_Ext(iot, "IoT Devices", "Vehicle sensors (GPS, battery, locks)")
    System_Ext(payment, "Payment Gateway", "Stripe/PayPal payment processing")
    System_Ext(notification, "Notification Service", "Push notifications, SMS, Email")
    System_Ext(maps, "Maps API", "Google Maps / OpenStreetMap")

    Rel(user, smp, "Rents vehicles, views locations", "HTTPS/REST")
    Rel(operator, smp, "Monitors fleet, manages vehicles", "HTTPS/REST")
    Rel(admin, smp, "Manages users, configures system", "HTTPS/REST")

    Rel(iot, smp, "Sends telemetry data", "HTTP/MQTT")
    Rel(smp, iot, "Controls locks, requests data", "HTTP/MQTT")

    Rel(smp, payment, "Processes payments", "HTTPS/REST")
    Rel(smp, notification, "Sends alerts", "HTTPS/REST")
    Rel(smp, maps, "Geocoding, routing", "HTTPS/REST")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Container Diagram

```mermaid
C4Container
    title Container Diagram - Shared Mobility Platform

    Person(user, "User", "Mobile app user")
    Person(ops, "Operations Team", "Fleet manager")

    System_Boundary(smp, "Shared Mobility Platform") {
        Container(apiGateway, "API Gateway", "Spring Cloud Gateway", "Routes requests, authentication, rate limiting")

        Container(userService, "User Service", "Spring Boot", "User authentication, profiles, JWT")
        Container(vehicleService, "Vehicle Service", "Spring Boot", "Vehicle inventory, IoT integration")
        Container(rentalService, "Rental Service", "Spring Boot", "Rental transactions, Saga orchestration")
        Container(locationService, "Location Service", "Spring Boot", "GPS tracking, geospatial queries")
        Container(batteryService, "Battery Service", "Spring Boot", "Battery monitoring, alerts")

        Container(kafka, "Kafka Cluster", "Apache Kafka", "Event streaming backbone")
        Container(kafkaUI, "Kafka UI", "Provectus Labs", "Kafka monitoring dashboard")

        ContainerDb(userDB, "User Database", "PostgreSQL", "User accounts, authentication")
        ContainerDb(vehicleDB, "Vehicle Database", "PostgreSQL", "Vehicle inventory, status")
        ContainerDb(rentalDB, "Rental Database", "PostgreSQL", "Rental transactions, pricing")
        ContainerDb(locationDB, "Location Database", "MongoDB", "GPS time-series data")
        ContainerDb(batteryDB, "Battery Database", "MongoDB", "Battery time-series logs")
        ContainerDb(redis, "Redis Cache", "Redis", "Saga state, location cache, idempotency")
    }

    System_Ext(iot, "IoT Devices", "Vehicle sensors")
    System_Ext(payment, "Payment Gateway", "Stripe")

    Rel(user, apiGateway, "Uses", "HTTPS/REST")
    Rel(ops, kafkaUI, "Monitors", "HTTPS")

    Rel(apiGateway, userService, "Routes to", "HTTP")
    Rel(apiGateway, vehicleService, "Routes to", "HTTP")
    Rel(apiGateway, rentalService, "Routes to", "HTTP")
    Rel(apiGateway, locationService, "Routes to", "HTTP")
    Rel(apiGateway, batteryService, "Routes to", "HTTP")

    Rel(userService, userDB, "Reads/Writes", "JDBC")
    Rel(vehicleService, vehicleDB, "Reads/Writes", "JDBC")
    Rel(rentalService, rentalDB, "Reads/Writes", "JDBC")
    Rel(locationService, locationDB, "Reads/Writes", "MongoDB Driver")
    Rel(batteryService, batteryDB, "Reads/Writes", "MongoDB Driver")

    Rel(rentalService, redis, "Saga state", "Lettuce")
    Rel(locationService, redis, "Cache location", "Lettuce")
    Rel(vehicleService, redis, "Idempotency", "Lettuce")

    Rel(rentalService, kafka, "Publishes events", "Kafka Producer")
    Rel(vehicleService, kafka, "Publishes events", "Kafka Producer")
    Rel(batteryService, kafka, "Publishes events", "Kafka Producer")

    Rel(kafka, vehicleService, "Consumes events", "Kafka Consumer")
    Rel(kafka, locationService, "Consumes events", "Kafka Consumer")
    Rel(kafka, batteryService, "Consumes events", "Kafka Consumer")

    Rel(iot, vehicleService, "Telemetry", "HTTP")
    Rel(iot, batteryService, "Telemetry", "HTTP")

    Rel(rentalService, payment, "Charge user", "HTTPS")

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

## Component Diagram - Rental Service (Example)

```mermaid
C4Component
    title Component Diagram - Rental Service

    Container_Boundary(rentalService, "Rental Service") {
        Component(rentalController, "RentalController", "Spring MVC", "REST API endpoints for rentals")
        Component(rentalService_comp, "RentalService", "Spring Service", "Business logic, orchestration")
        Component(sagaOrchestrator, "RentalSagaOrchestrator", "Saga Pattern", "Distributed transaction coordination")
        Component(eventPublisher, "KafkaEventPublisher", "Kafka Producer", "Publishes domain events")
        Component(rentalRepo, "RentalRepository", "Spring Data JPA", "Rental data access")
        Component(sagaRepo, "RentalSagaRepository", "Spring Data Redis", "Saga state persistence")
    }

    ContainerDb(rentalDB, "Rental Database", "PostgreSQL")
    ContainerDb(redis, "Redis", "Redis")
    Container(kafka, "Kafka", "Apache Kafka")

    Rel(rentalController, rentalService_comp, "Uses")
    Rel(rentalService_comp, sagaOrchestrator, "Starts saga")
    Rel(rentalService_comp, rentalRepo, "Reads/Writes")
    Rel(rentalService_comp, eventPublisher, "Publishes events")

    Rel(sagaOrchestrator, sagaRepo, "Saves saga state")

    Rel(rentalRepo, rentalDB, "JDBC", "SQL")
    Rel(sagaRepo, redis, "Lettuce", "GET/SET")
    Rel(eventPublisher, kafka, "Kafka Producer", "Async")
```

## Technology Stack

### Frontend (Not Shown)
- **Mobile App**: React Native / Flutter
- **Web Dashboard**: React / Vue.js
- **Real-time Updates**: WebSocket / Server-Sent Events

### Backend Microservices
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **API**: REST (Spring MVC)
- **Security**: Spring Security + JWT
- **Validation**: Hibernate Validator

### Databases
| Service | Database | Type | Purpose |
|---------|----------|------|---------|
| User Service | PostgreSQL | Relational | User accounts, authentication |
| Vehicle Service | PostgreSQL | Relational | Vehicle inventory, status |
| Rental Service | PostgreSQL | Relational | Rental transactions, pricing |
| Location Service | MongoDB | Document (Time-series) | GPS tracking data |
| Battery Service | MongoDB | Document (Time-series) | Battery telemetry logs |
| All Services | Redis | In-Memory Cache | Saga state, location cache, idempotency |

### Messaging & Event Streaming
- **Event Bus**: Apache Kafka 3.x
- **Event Format**: JSON (Jackson serialization)
- **Consumer Groups**: Shared-mobility-group
- **Topics**: 14 topics (vehicle.*, battery.*, location.*, rental.*, payment.*)
- **Monitoring**: Kafka UI (Provectus Labs)

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Orchestration** (Future): Kubernetes
- **Service Discovery** (Future): Eureka / Consul
- **API Gateway** (Future): Spring Cloud Gateway / Kong
- **Monitoring**: Prometheus + Grafana (not implemented)
- **Logging**: ELK Stack (not implemented)

## Communication Patterns

### Synchronous Communication (REST)
```
Client → API Gateway → Microservice → Database
                     ↓
                 Response (200 OK)
```

**Use Cases:**
- User login/registration
- Query vehicle availability
- Get rental history
- Fetch real-time location

**Characteristics:**
- Request-Response pattern
- Immediate consistency
- Blocking (waits for response)
- Timeout handling required

### Asynchronous Communication (Kafka Events)
```
Service A → Kafka Topic → Service B
                       → Service C (parallel)
                       → Service D (parallel)
```

**Use Cases:**
- Vehicle rented → Update vehicle status, start location tracking
- Vehicle returned → Update vehicle, record end location, check battery
- Battery low → Notify ops team, update vehicle status

**Characteristics:**
- Fire-and-Forget pattern
- Eventual consistency
- Non-blocking (async processing)
- Fault tolerance (message persistence)

## Data Flow - Vehicle Rental Example

```mermaid
flowchart TD
    A[User: Start Rental] -->|REST API| B[Rental Service]
    B -->|1. Save Rental| C[(PostgreSQL)]
    B -->|2. Start Saga| D[(Redis - Saga State)]
    B -->|3. Publish Event| E[Kafka: vehicle.rented]

    E -->|Consume| F[Vehicle Service]
    E -->|Consume| G[Location Service]

    F -->|Update Status| H[(PostgreSQL - Vehicle DB)]
    G -->|Insert Location| I[(MongoDB - Location DB)]
    G -->|Cache Location| J[(Redis - Cache)]

    B -->|4. Return Response| K[User: Rental Confirmed]

    style A fill:#e1f5ff
    style K fill:#c8e6c9
    style E fill:#fff9c4
    style D fill:#ffccbc
    style J fill:#ffccbc
```

## Deployment Architecture (Docker Compose)

```mermaid
graph TB
    subgraph "Docker Network: smp-network"
        subgraph "Microservices"
            US[user-service:8081]
            VS[vehicle-service:8082]
            RS[rental-service:8083]
            LS[location-service:8084]
            BS[battery-service:8085]
        end

        subgraph "Infrastructure"
            PG1[(PostgreSQL:5432<br/>User DB)]
            PG2[(PostgreSQL:5433<br/>Vehicle DB)]
            PG3[(PostgreSQL:5434<br/>Rental DB)]
            MG1[(MongoDB:27017<br/>Location DB)]
            MG2[(MongoDB:27018<br/>Battery DB)]
            RD[(Redis:6379)]
        end

        subgraph "Event Streaming"
            ZK[Zookeeper:2181]
            KF[Kafka:9092]
            KUI[Kafka UI:8090]
        end
    end

    US -.-> PG1
    VS -.-> PG2
    RS -.-> PG3
    LS -.-> MG1
    BS -.-> MG2

    RS -.-> RD
    LS -.-> RD

    RS --> KF
    VS --> KF
    BS --> KF

    KF --> VS
    KF --> LS
    KF --> BS

    KF -.-> ZK
    KUI -.-> KF

    style US fill:#4FC3F7
    style VS fill:#4FC3F7
    style RS fill:#4FC3F7
    style LS fill:#4FC3F7
    style BS fill:#4FC3F7

    style PG1 fill:#81C784
    style PG2 fill:#81C784
    style PG3 fill:#81C784
    style MG1 fill:#FFB74D
    style MG2 fill:#FFB74D
    style RD fill:#E57373

    style KF fill:#FFF176
    style ZK fill:#FFF59D
    style KUI fill:#FFEB3B
```

## Network Ports

| Service | Internal Port | External Port | Protocol |
|---------|--------------|---------------|----------|
| User Service | 8081 | 8081 | HTTP |
| Vehicle Service | 8082 | 8082 | HTTP |
| Rental Service | 8083 | 8083 | HTTP |
| Location Service | 8084 | 8084 | HTTP |
| Battery Service | 8085 | 8085 | HTTP |
| PostgreSQL (User) | 5432 | 5432 | PostgreSQL |
| PostgreSQL (Vehicle) | 5432 | 5433 | PostgreSQL |
| PostgreSQL (Rental) | 5432 | 5434 | PostgreSQL |
| MongoDB (Location) | 27017 | 27017 | MongoDB |
| MongoDB (Battery) | 27017 | 27018 | MongoDB |
| Redis | 6379 | 6379 | Redis |
| Zookeeper | 2181 | 2181 | Zookeeper |
| Kafka | 9092 | 9092 | Kafka |
| Kafka UI | 8080 | 8090 | HTTP |

## Scalability Architecture

### Horizontal Scaling Strategy

```mermaid
graph LR
    subgraph "Load Balancer"
        LB[Nginx / AWS ALB]
    end

    subgraph "Rental Service Instances"
        R1[rental-1]
        R2[rental-2]
        R3[rental-3]
    end

    subgraph "Kafka Partitions"
        K1[Partition 0]
        K2[Partition 1]
        K3[Partition 2]
    end

    subgraph "Database"
        DB[(PostgreSQL<br/>Master-Replica)]
    end

    LB --> R1
    LB --> R2
    LB --> R3

    R1 --> K1
    R2 --> K2
    R3 --> K3

    R1 -.-> DB
    R2 -.-> DB
    R3 -.-> DB

    style LB fill:#4DD0E1
    style R1 fill:#81C784
    style R2 fill:#81C784
    style R3 fill:#81C784
    style K1 fill:#FFF176
    style K2 fill:#FFF176
    style K3 fill:#FFF176
```

### Scaling Characteristics

| Component | Scaling Strategy | Max Instances | Bottleneck |
|-----------|-----------------|---------------|------------|
| User Service | Horizontal | 10+ | PostgreSQL write throughput |
| Vehicle Service | Horizontal | 10+ | PostgreSQL write throughput |
| Rental Service | Horizontal | 10+ | Redis connections, Kafka producer |
| Location Service | Horizontal | 20+ | MongoDB write throughput |
| Battery Service | Horizontal | 20+ | MongoDB write throughput |
| PostgreSQL | Master-Replica | 1 master + N replicas | Master write IOPS |
| MongoDB | Sharding | Unlimited | Network bandwidth |
| Redis | Cluster | 3-6 nodes | Memory |
| Kafka | Partitioning | 10+ brokers | Disk I/O |

## High Availability & Fault Tolerance

### Service Resilience Patterns

```mermaid
graph TD
    A[Client Request] --> B{Circuit Breaker}
    B -->|Closed| C[Service Call]
    B -->|Open| D[Fallback Response]

    C -->|Success| E[Return Response]
    C -->|Failure| F{Retry Logic}

    F -->|Max Retries| D
    F -->|Retry| C

    E --> G[Update Metrics]
    D --> G

    style B fill:#FFB74D
    style D fill:#E57373
    style F fill:#FFF176
```

### Implemented Resilience Mechanisms

1. **Idempotency** (✅ Implemented)
   - Redis-backed idempotency checker
   - Prevents duplicate event processing
   - 7-day TTL for processed event IDs

2. **Saga Pattern** (✅ Implemented)
   - Compensating transactions for failures
   - Distributed transaction coordination
   - State persistence in Redis (24h TTL)

3. **Event Replay** (✅ Via Kafka)
   - Kafka retains messages (168 hours = 7 days)
   - Can replay events from any offset
   - Useful for disaster recovery

4. **Database Replication** (⏳ Future)
   - PostgreSQL master-replica setup
   - MongoDB replica set (3 nodes)
   - Read-write split for scalability

5. **Circuit Breaker** (⏳ Future)
   - Resilience4j integration
   - Fallback for external service calls
   - Automatic recovery

6. **Health Checks** (⏳ Future)
   - Spring Boot Actuator
   - Kubernetes liveness/readiness probes
   - Dependency health monitoring

## Security Architecture

### Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant App
    participant UserService
    participant JWT as JWT Provider
    participant Service as Other Services

    User->>App: Login (email, password)
    App->>UserService: POST /auth/login
    UserService->>UserService: Validate credentials
    UserService->>JWT: Generate token
    JWT-->>UserService: JWT (24h expiry)
    UserService-->>App: Return token + user info
    App->>App: Store token in secure storage

    App->>Service: API request with Authorization header
    Service->>JWT: Validate token
    JWT-->>Service: Token valid, extract user info
    Service-->>App: Return response
```

### Security Layers

| Layer | Mechanism | Implementation |
|-------|-----------|----------------|
| **Transport** | HTTPS/TLS | SSL certificates (production) |
| **Authentication** | JWT | Spring Security + JWT |
| **Authorization** | Role-based (RBAC) | UserRole enum (CUSTOMER, OPERATOR, ADMIN) |
| **API Security** | Rate limiting | API Gateway (future) |
| **Database** | Connection pooling, encrypted passwords | HikariCP, BCrypt |
| **Event Bus** | SASL/SSL | Kafka ACLs (future) |
| **Secrets** | Environment variables | Docker secrets, AWS Secrets Manager (future) |

## Monitoring & Observability (Future)

### Planned Observability Stack

```mermaid
graph TB
    subgraph "Services"
        S1[Service 1]
        S2[Service 2]
        S3[Service 3]
    end

    subgraph "Metrics Collection"
        PROM[Prometheus]
    end

    subgraph "Visualization"
        GRAF[Grafana]
    end

    subgraph "Logging"
        ES[Elasticsearch]
        LS[Logstash]
        KB[Kibana]
    end

    subgraph "Tracing"
        JAE[Jaeger]
    end

    S1 -->|Metrics| PROM
    S2 -->|Metrics| PROM
    S3 -->|Metrics| PROM

    PROM --> GRAF

    S1 -->|Logs| LS
    S2 -->|Logs| LS
    S3 -->|Logs| LS

    LS --> ES
    ES --> KB

    S1 -->|Traces| JAE
    S2 -->|Traces| JAE
    S3 -->|Traces| JAE

    style PROM fill:#E57373
    style GRAF fill:#81C784
    style ES fill:#4FC3F7
    style JAE fill:#FFB74D
```

### Key Metrics to Monitor

**Service Metrics:**
- Request rate (req/s)
- Response time (p50, p95, p99)
- Error rate (%)
- JVM metrics (heap, GC)

**Business Metrics:**
- Active rentals count
- Revenue per hour
- Average trip duration
- Vehicle utilization rate

**Infrastructure Metrics:**
- Database connections
- Kafka consumer lag
- Redis memory usage
- MongoDB write throughput

## Related Files

### Infrastructure Configuration
- `/docker-compose.yml` - Full stack deployment
- `/services/*/src/main/resources/application.yml` - Service configurations

### Network Configuration
- Kafka: `kafka:9092` (internal), `localhost:29092` (host)
- All services on `smp-network` Docker bridge network

## Architecture Decision Records (ADRs)

See `docs/architecture-decisions.md` for detailed rationale on:
- Why microservices over monolith
- Why Kafka over RabbitMQ
- Why MongoDB for time-series data
- Why Saga over 2PC
- Why Redis for caching and saga state
