# Shared Mobility Platform

Event-Driven Microservices Architecture for Shared Mobility Management

## 📋 Project Overview

A scalable, event-driven microservices platform for managing shared mobility vehicles (e-scooters, e-bikes, bicycles) with real-time tracking, battery management, and multi-vendor IoT device integration.

### Key Features

- **5 Microservices**: User, Vehicle, Rental, Location, Battery
- **Event-Driven Architecture**: Apache Kafka for service communication
- **Hexagonal Architecture**: IoT device abstraction with HTTP/Mock adapters
- **Real-time Data**: MongoDB time-series for location and battery logs
- **High Performance**: Redis caching (30s TTL for locations)
- **Fault Isolation**: Independent service deployment and scaling

## 🏗️ Architecture

### Microservices

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| User Service | 8081 | PostgreSQL | Authentication, JWT, User management |
| Vehicle Service | 8082 | PostgreSQL | Vehicle CRUD, IoT control (Hexagonal) |
| Rental Service | 8083 | PostgreSQL | Rental transactions, Event publishing |
| Location Service | 8084 | MongoDB | GPS tracking, Geospatial queries |
| Battery Service | 8085 | MongoDB | Battery monitoring, Low-battery alerts |

### Infrastructure

- **PostgreSQL**: Transactional data (Users, Vehicles, Rentals)
- **MongoDB**: Time-series data (Locations, Battery logs)
- **Apache Kafka**: Event streaming (Vehicle rented/returned, Battery low)
- **Redis**: Location caching (10x performance improvement)
- **Zookeeper**: Kafka coordination
- **Kafka UI**: Web management interface (Port 8090)

### Architecture Patterns

✅ **Microservices**: Independent services with separate databases
✅ **Event-Driven**: Kafka for async communication and loose coupling
✅ **Hexagonal (Ports & Adapters)**: IoT device abstraction in Vehicle Service
✅ **CQRS**: Read/Write separation in Location Service
✅ **Repository Pattern**: Data access abstraction
✅ **Saga Pattern**: Distributed transaction management (Rental flow)

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21
- Gradle 8.11

### 1. Start Infrastructure

```bash
# Start PostgreSQL, MongoDB, Kafka, Redis
docker-compose up -d

# Verify services are healthy
docker-compose ps

# View Kafka UI
open http://localhost:8090
```

### 2. Build All Services

```bash
./gradlew clean build -x test
```

### 3. Run Services

**Option A: Run all services**
```bash
# Terminal 1 - User Service
./gradlew :services:user-service:bootRun

# Terminal 2 - Vehicle Service
./gradlew :services:vehicle-service:bootRun

# Terminal 3 - Rental Service
./gradlew :services:rental-service:bootRun

# Terminal 4 - Location Service
./gradlew :services:location-service:bootRun

# Terminal 5 - Battery Service
./gradlew :services:battery-service:bootRun
```

**Option B: Build and run JARs**
```bash
# Build JARs
./gradlew build -x test

# Run services
java -jar services/user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar &
java -jar services/vehicle-service/build/libs/vehicle-service-0.0.1-SNAPSHOT.jar &
java -jar services/rental-service/build/libs/rental-service-0.0.1-SNAPSHOT.jar &
java -jar services/location-service/build/libs/location-service-0.0.1-SNAPSHOT.jar &
java -jar services/battery-service/build/libs/battery-service-0.0.1-SNAPSHOT.jar &
```

### 4. Verify Services

```bash
# Check health endpoints
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Vehicle Service
curl http://localhost:8083/actuator/health  # Rental Service
curl http://localhost:8084/actuator/health  # Location Service
curl http://localhost:8085/actuator/health  # Battery Service
```

## 📡 API Examples

### User Service (8081)

**Register User**
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+821012345678"
  }'
```

**Login**
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Vehicle Service (8082)

**Get Available Vehicles**
```bash
curl http://localhost:8082/api/v1/vehicles/available
```

**Lock/Unlock Vehicle** (via IoT adapters)
```bash
curl -X POST http://localhost:8082/api/v1/vehicles/{vehicleId}/lock
curl -X POST http://localhost:8082/api/v1/vehicles/{vehicleId}/unlock
```

### Rental Service (8083)

**Start Rental**
```bash
curl -X POST "http://localhost:8083/api/v1/rentals/start?userId=USER_ID&vehicleId=VEHICLE_ID&lat=37.5665&lon=126.9780&batteryLevel=85"
```

**End Rental**
```bash
curl -X POST "http://localhost:8083/api/v1/rentals/{rentalId}/end?lat=37.5700&lon=126.9800&batteryLevel=65"
```

### Location Service (8084)

**Get Latest Location**
```bash
curl http://localhost:8084/api/v1/locations/vehicle/{vehicleId}/latest
```

### Battery Service (8085)

**Get Latest Battery Status**
```bash
curl http://localhost:8085/api/v1/battery/vehicle/{vehicleId}/latest
```

## 🔄 Event Flow

### Rental Flow (Event-Driven)

```
1. User starts rental → Rental Service
2. Rental Service publishes "VehicleRentedEvent" to Kafka
3. Event consumed by:
   - Vehicle Service: Updates status to IN_USE
   - Location Service: Tracks rental start location
   - Battery Service: Records start battery level

4. User ends rental → Rental Service
5. Rental Service publishes "VehicleReturnedEvent" to Kafka
6. Event consumed by:
   - Vehicle Service: Updates status to AVAILABLE
   - Location Service: Tracks return location
   - Battery Service: Records end battery level, checks if low
```

### Battery Monitoring

```
Battery Service detects battery < 20%
→ Publishes "BatteryLowEvent" to Kafka
→ Operations team receives alert
→ Vehicle marked for charging
```

## 🛠️ Technology Stack

**Backend**: Java 21, Spring Boot 3.5.7
**Build**: Gradle 8.11
**Databases**: PostgreSQL 16, MongoDB 7
**Message Queue**: Apache Kafka 7.6
**Cache**: Redis 7
**Security**: JWT (JJWT 0.12.6), BCrypt
**Monitoring**: Prometheus, Micrometer
**Containerization**: Docker, Docker Compose

## 📊 Performance Targets

- Average response time: **<1s** (under 100 concurrent requests)
- Location query: **0.3s** (10x improvement with Redis cache)
- Event processing success rate: **>95%**
- Concurrent user capacity: **10,000+**

## 🔍 Monitoring

**Prometheus Metrics**
```bash
# User Service metrics
curl http://localhost:8081/actuator/prometheus

# All services expose /actuator/prometheus endpoint
```

**Kafka UI**
- URL: http://localhost:8090
- Monitor topics, consumer groups, message flow

## 📁 Project Structure

```
shared-mobility-platform/
├── common/                    # Shared modules
│   ├── common-domain/        # Domain models, DTOs, Exceptions
│   ├── common-event/         # Kafka events, Publisher, Consumer
│   └── common-utils/         # Utilities (Validation, Distance calc)
├── services/                  # Microservices
│   ├── user-service/         # Authentication & User management
│   ├── vehicle-service/      # Vehicle CRUD + IoT adapters
│   ├── rental-service/       # Rental transactions + Events
│   ├── location-service/     # GPS tracking (MongoDB)
│   └── battery-service/      # Battery monitoring (MongoDB)
├── infrastructure/            # Infrastructure components
│   ├── api-gateway/          # Spring Cloud Gateway
│   └── iot-simulator/        # IoT device simulator
├── docker/                    # Docker configurations
│   └── init-scripts/         # DB initialization scripts
├── docker-compose.yml         # Infrastructure orchestration
└── README.md                  # This file
```

## 🔐 Security

- **JWT Authentication**: 24-hour token expiration
- **BCrypt Password Hashing**: Secure password storage
- **Stateless Sessions**: No server-side session storage
- **Environment Variables**: Sensitive data externalized

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :services:user-service:test

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📚 Documentation

- [Architecture Overview](docs/swa-draft-script.md)
- [Docker Setup Guide](docker/README.md)
- API Documentation: Swagger UI (when API Gateway is added)

## 🤝 Contributing

This is an educational project demonstrating enterprise-level architecture patterns for IoT-based shared mobility platforms.

## 📜 License

Educational/Portfolio Project

---

**🤖 Generated with Claude Code**

Event-Driven Microservices | Hexagonal Architecture | Real-time IoT Data
