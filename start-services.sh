#!/bin/bash

# Shared Mobility Platform - Service Startup Script

echo "==================================="
echo "Shared Mobility Platform"
echo "Starting all microservices..."
echo "==================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if infrastructure is running
echo -e "${BLUE}Checking infrastructure...${NC}"
if ! docker ps | grep -q smp-postgres; then
    echo "Infrastructure not running. Starting Docker Compose..."
    docker-compose up -d
    echo "Waiting for services to be ready (30 seconds)..."
    sleep 30
fi

echo -e "${GREEN}✓ Infrastructure ready${NC}"

# Build all services
echo -e "${BLUE}Building services...${NC}"
./gradlew build -x test

echo -e "${GREEN}✓ Build complete${NC}"

# Start services in background
echo -e "${BLUE}Starting microservices...${NC}"

java -jar services/user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar > logs/user-service.log 2>&1 &
echo -e "${GREEN}✓ User Service started (port 8081)${NC}"
sleep 5

java -jar services/vehicle-service/build/libs/vehicle-service-0.0.1-SNAPSHOT.jar > logs/vehicle-service.log 2>&1 &
echo -e "${GREEN}✓ Vehicle Service started (port 8082)${NC}"
sleep 5

java -jar services/rental-service/build/libs/rental-service-0.0.1-SNAPSHOT.jar > logs/rental-service.log 2>&1 &
echo -e "${GREEN}✓ Rental Service started (port 8083)${NC}"
sleep 5

java -jar services/location-service/build/libs/location-service-0.0.1-SNAPSHOT.jar > logs/location-service.log 2>&1 &
echo -e "${GREEN}✓ Location Service started (port 8084)${NC}"
sleep 5

java -jar services/battery-service/build/libs/battery-service-0.0.1-SNAPSHOT.jar > logs/battery-service.log 2>&1 &
echo -e "${GREEN}✓ Battery Service started (port 8085)${NC}"

echo ""
echo "==================================="
echo -e "${GREEN}All services started!${NC}"
echo "==================================="
echo ""
echo "Service URLs:"
echo "  User Service:     http://localhost:8081/api/v1"
echo "  Vehicle Service:  http://localhost:8082/api/v1"
echo "  Rental Service:   http://localhost:8083/api/v1"
echo "  Location Service: http://localhost:8084/api/v1"
echo "  Battery Service:  http://localhost:8085/api/v1"
echo ""
echo "Infrastructure:"
echo "  Kafka UI:        http://localhost:8090"
echo "  PostgreSQL:      localhost:5432"
echo "  MongoDB:         localhost:27017"
echo "  Redis:           localhost:6379"
echo ""
echo "Logs are available in the logs/ directory"
echo ""
echo "To stop all services: ./stop-services.sh"
