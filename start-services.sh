#!/bin/bash

# Shared Mobility Platform - Service Startup Script

echo "==================================="
echo "Shared Mobility Platform"
echo "Starting all microservices..."
echo "==================================="

# Check if infrastructure is running
echo "Checking infrastructure..."
if ! docker ps | grep -q smp-postgres; then
    echo "Infrastructure not running. Starting Docker Compose..."
    docker-compose up -d
    echo "Waiting for services to be ready (30 seconds)..."
    sleep 30
fi

echo "✓ Infrastructure ready"

# Build all services
echo "Building services..."
./gradlew build -x test

echo "✓ Build complete"

# Start services in background
echo "Starting microservices..."

java -jar services/user-service/build/libs/user-service.jar > logs/user-service.log 2>&1 &
echo "✓ User Service started (port 8081)"
sleep 5

java -jar services/vehicle-service/build/libs/vehicle-service.jar > logs/vehicle-service.log 2>&1 &
echo "✓ Vehicle Service started (port 8082)"
sleep 5

java -jar services/rental-service/build/libs/rental-service.jar > logs/rental-service.log 2>&1 &
echo "✓ Rental Service started (port 8083)"
sleep 5

java -jar services/location-service/build/libs/location-service.jar > logs/location-service.log 2>&1 &
echo "✓ Location Service started (port 8084)"
sleep 5

java -jar services/battery-service/build/libs/battery-service.jar > logs/battery-service.log 2>&1 &
echo "✓ Battery Service started (port 8085)"

echo ""
echo "==================================="
echo "All services started!"
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
