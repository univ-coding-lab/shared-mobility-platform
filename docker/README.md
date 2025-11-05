# Docker Infrastructure Setup

This directory contains Docker Compose configuration for the Shared Mobility Platform infrastructure.

## Services

### Databases
- **PostgreSQL** (Port 5432): Transactional data storage
  - user_service_db
  - vehicle_service_db
  - rental_service_db

- **MongoDB** (Port 27017): IoT time-series data
  - location_logs (with geospatial indexing)
  - battery_logs
  - vehicle_events

### Message Queue
- **Apache Kafka** (Port 29092): Event streaming platform
- **Zookeeper** (Port 2181): Kafka coordination service
- **Kafka UI** (Port 8090): Web interface for Kafka management

### Caching
- **Redis** (Port 6379): In-memory cache
  - Vehicle location caching (30s TTL)
  - Vehicle status caching (10s TTL)

## Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Start All Services
```bash
# Start all infrastructure services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean reset)
docker-compose down -v
```

### Individual Service Management
```bash
# Start specific service
docker-compose up -d postgres

# Restart specific service
docker-compose restart kafka

# Stop specific service
docker-compose stop redis
```

## Health Checks

All services include health checks. Verify status:
```bash
docker-compose ps
```

## Access Points

- **PostgreSQL**: `localhost:5432`
  - Username: `smpuser`
  - Password: `smppass`

- **MongoDB**: `localhost:27017`
  - Username: `root`
  - Password: `rootpass`

- **Redis**: `localhost:6379`
  - Password: `redispass`

- **Kafka**: `localhost:29092`
- **Kafka UI**: http://localhost:8090

## Database Initialization

### PostgreSQL
- Automatically creates separate databases for each service
- Creates schemas: `users`, `vehicles`, `rentals`
- Script: `init-scripts/postgres/01-init-databases.sql`

### MongoDB
- Creates time-series collections for IoT data
- Sets up geospatial indexes for location queries
- Creates application user `iotuser`
- Script: `init-scripts/mongodb/01-init-collections.js`

## Troubleshooting

### Services not starting
```bash
# Check logs for errors
docker-compose logs [service-name]

# Verify ports are not in use
netstat -an | grep LISTEN | grep -E '5432|27017|6379|29092'
```

### Clean restart
```bash
# Remove all containers and volumes
docker-compose down -v

# Remove unused volumes
docker volume prune

# Restart
docker-compose up -d
```

### Connect to databases
```bash
# PostgreSQL
docker exec -it smp-postgres psql -U smpuser -d user_service_db

# MongoDB
docker exec -it smp-mongodb mongosh -u root -p rootpass

# Redis
docker exec -it smp-redis redis-cli -a redispass
```

## Production Considerations

For production deployment:
1. Change all default passwords in `.env`
2. Use Docker secrets for sensitive data
3. Configure volume backups
4. Set up log rotation
5. Implement monitoring (Prometheus + Grafana)
6. Consider Kubernetes for orchestration
7. Enable SSL/TLS for all connections
8. Configure resource limits (CPU, memory)
