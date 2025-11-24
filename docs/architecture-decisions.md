# Architecture Decision Records (ADRs)

This document records key architectural decisions made in the Shared Mobility Platform, including the context, rationale, and trade-offs.

---

## ADR-001: Microservices Architecture

**Status**: Accepted
**Date**: 2024-01
**Decision Makers**: Architecture Team

### Context
Need to build a scalable vehicle sharing platform with different data access patterns (transactional vs time-series).

### Decision
Adopt microservices architecture with 5 independent services:
- User Service (authentication, profiles)
- Vehicle Service (inventory, IoT)
- Rental Service (transactions, saga)
- Location Service (GPS tracking)
- Battery Service (telemetry monitoring)

### Rationale
1. **Independent Scaling**: Location/Battery services need 4x capacity of User/Vehicle
2. **Technology Diversity**: Use PostgreSQL for transactions, MongoDB for time-series
3. **Team Autonomy**: Different teams can own different services
4. **Fault Isolation**: Battery Service down doesn't affect rentals

### Consequences
**Positive**:
- Horizontal scaling per service
- Polyglot persistence (SQL + NoSQL)
- Independent deployment cycles

**Negative**:
- Distributed system complexity
- Need event-driven communication
- Saga pattern for distributed transactions

### Alternatives Considered
**Monolithic Architecture**: Simpler but can't scale components independently
**Modular Monolith**: Better than pure monolith, but still single deployment unit

---

## ADR-002: Event-Driven Architecture with Kafka

**Status**: Accepted
**Date**: 2024-01

### Context
Microservices need to communicate without tight coupling and synchronous REST calls.

### Decision
Use Apache Kafka as event backbone with 14 domain event topics.

### Rationale
1. **Loose Coupling**: Services don't know about each other, only events
2. **Asynchronous Processing**: Rental completion doesn't wait for vehicle update
3. **Event Replay**: 7-day retention allows recovery from failures
4. **Scalability**: Partitioning by vehicleId enables parallel processing

### Consequences
**Positive**:
- Services independently scalable
- Failure isolation (consumer down doesn't affect publisher)
- Audit trail (immutable event log)

**Negative**:
- Eventual consistency (not immediate)
- Need idempotency handling
- Operational complexity (Kafka cluster management)

### Alternatives Considered
**RabbitMQ**: Simpler but lower throughput, less suited for event sourcing
**AWS SNS/SQS**: Cloud-specific, vendor lock-in
**REST APIs**: Tight coupling, synchronous, no event replay

---

## ADR-003: Database Per Service Pattern

**Status**: Accepted
**Date**: 2024-01

### Context
Microservices should not share databases to avoid tight coupling.

### Decision
Each service owns its database:
- User/Vehicle/Rental → PostgreSQL (separate instances)
- Location/Battery → MongoDB (separate databases)
- All services → Redis (shared for caching/saga)

### Rationale
1. **Service Autonomy**: Schema changes don't affect other services
2. **Technology Fit**: PostgreSQL for ACID, MongoDB for time-series
3. **Independent Scaling**: Each database scales independently

### Consequences
**Positive**:
- No cross-service SQL joins (enforces boundaries)
- Optimal database choice per service
- Independent backup/restore strategies

**Negative**:
- Data duplication (vehicle location in both Vehicle and Location services)
- Need Saga pattern for distributed transactions
- Higher infrastructure cost (6 databases)

---

## ADR-004: Saga Pattern for Distributed Transactions

**Status**: Accepted
**Date**: 2024-01

### Context
Rental spans multiple services (Rental, Vehicle, Payment, Location) but no distributed transactions.

### Decision
Use Saga orchestration pattern with `RentalSagaOrchestrator`.

### Rationale
1. **Microservices Compatible**: No 2PC (two-phase commit) required
2. **Clear Rollback**: Compensating actions well-defined
3. **State Persistence**: Saga state in Redis for fault tolerance

### Consequences
**Positive**:
- Eventual consistency acceptable for rental domain
- Easy to add new saga steps
- Clear failure handling

**Negative**:
- Complexity of compensation logic
- Partial failures visible to users
- Need retry mechanisms

### Alternatives Considered
**Two-Phase Commit (2PC)**: Not supported in microservices, high latency
**Choreography Saga**: Harder to track state, no central coordinator

---

## ADR-005: MongoDB for Time-Series Data

**Status**: Accepted
**Date**: 2024-01

### Context
Location and Battery services store high-volume time-series telemetry data.

### Decision
Use MongoDB for Location and Battery services.

### Rationale
1. **Geospatial Indexing**: Native 2dsphere index for proximity queries
2. **Write Throughput**: 10K+ writes/sec with sharding
3. **Schema Flexibility**: IoT payloads vary (voltage, temperature, etc.)
4. **Time-Series**: Efficient time-range queries

### Consequences
**Positive**:
- Fast geospatial queries (find vehicles within 1 km)
- High insert performance (append-only data)
- Horizontal scaling via sharding

**Negative**:
- No ACID transactions (acceptable for telemetry)
- Learning curve for geospatial queries
- Different from PostgreSQL (polyglot persistence)

### Alternatives Considered
**PostgreSQL with PostGIS**: More familiar but lower write throughput
**TimescaleDB**: Good for time-series but no native geospatial index
**Cassandra**: Higher throughput but no geospatial support

---

## ADR-006: Redis for Caching and Saga State

**Status**: Accepted
**Date**: 2024-01

### Context
Need low-latency access to saga state, location cache, and idempotency tracking.

### Decision
Use Redis for:
- Saga state (24h TTL)
- Location cache (30s TTL)
- Idempotency tracking (7d TTL)

### Rationale
1. **Performance**: Sub-millisecond latency
2. **TTL Support**: Automatic cleanup of stale data
3. **Atomic Operations**: SETNX for idempotency, INCR for counters

### Consequences
**Positive**:
- Fast saga state access (~1ms vs 30ms PostgreSQL)
- Location cache reduces MongoDB reads by 95%
- Idempotency without database overhead

**Negative**:
- Data loss if Redis crashes (saga state recreated from events)
- Memory limits (cache eviction needed)
- Not durable (use Redis persistence in production)

### Alternatives Considered
**PostgreSQL for Saga**: Durable but slower, no TTL
**Memcached for Cache**: No persistence, less feature-rich

---

## ADR-007: Idempotent Consumer Pattern

**Status**: Accepted
**Date**: 2024-01

### Context
Kafka guarantees at-least-once delivery, which can cause duplicate events.

### Decision
Implement `IdempotencyChecker` with Redis SETNX.

### Rationale
1. **Exactly-Once Semantics**: Prevent duplicate processing
2. **Atomic Check-and-Set**: Redis SETNX provides atomicity
3. **TTL-Based Cleanup**: 7-day TTL balances memory vs replay window

### Consequences
**Positive**:
- Safe with Kafka at-least-once delivery
- No duplicate vehicle status updates
- Automatic cleanup via TTL

**Negative**:
- Redis dependency for event processing
- 7-day window (events older than that can be reprocessed)

---

## ADR-008: Hexagonal Architecture for IoT Integration

**Status**: Accepted
**Date**: 2024-01

### Context
Vehicle Service integrates with IoT devices, but vendor may change.

### Decision
Use Ports & Adapters pattern with `IoTDevicePort` interface.

### Rationale
1. **Vendor Independence**: Easy to swap IoT providers
2. **Testability**: Use MockAdapter without real devices
3. **Dependency Inversion**: VehicleService depends on interface, not implementation

### Consequences
**Positive**:
- Swap MockAdapter ↔ HttpAdapter ↔ MQTTAdapter via @Qualifier
- Unit tests don't need real IoT devices
- Clear boundary between domain and infrastructure

**Negative**:
- Additional abstraction layer
- Need to maintain multiple adapters

---

## ADR-009: Spring Boot for Microservices

**Status**: Accepted
**Date**: 2024-01

### Context
Need Java framework for rapid microservice development.

### Decision
Use Spring Boot 3.x for all services.

### Rationale
1. **Productivity**: Auto-configuration, starter dependencies
2. **Ecosystem**: Spring Data, Spring Security, Spring Kafka
3. **Observability**: Actuator for health checks, metrics

### Consequences
**Positive**:
- Fast development (minimal boilerplate)
- Mature ecosystem (battle-tested libraries)
- Production-ready (health checks, metrics)

**Negative**:
- Higher memory footprint (vs Micronaut, Quarkus)
- Slower startup time (vs native compilation)

### Alternatives Considered
**Micronaut**: Lower memory, faster startup but smaller ecosystem
**Quarkus**: Native compilation but more complex
**Node.js**: Faster for I/O but lacks strong typing

---

## ADR-010: JWT for Authentication

**Status**: Accepted
**Date**: 2024-01

### Context
Need stateless authentication for microservices.

### Decision
Use JWT tokens (24h expiry) issued by User Service.

### Rationale
1. **Stateless**: No session storage needed
2. **Decentralized**: Each service validates token independently
3. **Claims-Based**: Embed user role, ID in token

### Consequences
**Positive**:
- Scalable (no session storage)
- Fast validation (signature check)
- Works with API Gateway

**Negative**:
- Token revocation hard (need blacklist)
- Sensitive data in token (base64 encoded, not encrypted)
- Longer than session ID (bandwidth overhead)

### Alternatives Considered
**Session Cookies**: Requires centralized session store (Redis)
**OAuth2**: Overkill for internal services

---

## ADR-011: Docker Compose for Local Development

**Status**: Accepted
**Date**: 2024-01

### Context
Need consistent development environment across team.

### Decision
Use Docker Compose for all infrastructure (PostgreSQL, MongoDB, Redis, Kafka).

### Rationale
1. **Consistency**: Same environment for all developers
2. **Isolation**: No global installs (PostgreSQL, MongoDB, etc.)
3. **Orchestration**: Start all services with `docker-compose up`

### Consequences
**Positive**:
- No "works on my machine" issues
- Easy onboarding (one command to start)
- Production-like environment (containerized)

**Negative**:
- Requires Docker knowledge
- Higher resource usage (all services running)
- Not production deployment (use Kubernetes)

---

## ADR-012: Manual Kafka Acknowledgment

**Status**: Accepted
**Date**: 2024-01

### Context
Need control over when Kafka consumer offset is committed.

### Decision
Use manual acknowledgment mode (`AckMode.MANUAL`).

### Rationale
1. **Transactional Safety**: Acknowledge after DB write succeeds
2. **Failure Handling**: Don't commit if processing fails
3. **Idempotency Integration**: Acknowledge after idempotency check

### Consequences
**Positive**:
- No message loss (re-delivered if processing fails)
- Explicit control over commit timing
- Works with idempotency pattern

**Negative**:
- Must call `acknowledgment.acknowledge()` explicitly
- Risk of consumer lag if acknowledgment forgotten

---

## ADR-013: Optimistic Locking for Concurrent Updates

**Status**: Accepted
**Date**: 2024-01

### Context
Multiple requests may update same vehicle/rental concurrently.

### Decision
Use JPA `@Version` for optimistic locking.

### Rationale
1. **No Locks**: Better concurrency than pessimistic locks
2. **Lost Update Prevention**: Prevents last-write-wins problem
3. **Automatic**: JPA manages version field

### Consequences
**Positive**:
- High concurrency (no database locks)
- Prevents data corruption
- Retry logic handles version conflicts

**Negative**:
- Client must retry on conflict
- Not suitable for high-contention scenarios

### Alternatives Considered
**Pessimistic Locking**: Lower concurrency, deadlock risk
**No Locking**: Lost updates possible

---

## Summary of Technology Choices

| Decision | Technology | Reason |
|----------|-----------|--------|
| Microservices | Spring Boot | Productivity, ecosystem |
| Event Bus | Kafka | Throughput, event sourcing |
| Transactional DB | PostgreSQL | ACID, relational integrity |
| Time-Series DB | MongoDB | Geospatial, write throughput |
| Cache/Saga | Redis | Performance, TTL support |
| Authentication | JWT | Stateless, decentralized |
| Container | Docker | Consistency, isolation |
| Saga | Orchestration | Clear state, compensation |
| IoT Integration | Hexagonal Arch | Vendor independence, testability |
| Locking | Optimistic | Concurrency, no deadlocks |

---

**Document Version**: 1.0
**Last Updated**: 2025-01-24
**Next Review**: 2025-04 (or when significant architectural changes proposed)
