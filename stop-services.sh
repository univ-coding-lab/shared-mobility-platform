#!/bin/bash

# Shared Mobility Platform - Service Shutdown Script

echo "==================================="
echo "Stopping all services..."
echo "==================================="

# Find and kill Java processes running our services
pkill -f "user-service"
pkill -f "vehicle-service"
pkill -f "rental-service"
pkill -f "location-service"
pkill -f "battery-service"

echo "✓ All microservices stopped"

# Optionally stop Docker infrastructure
read -p "Stop Docker infrastructure (PostgreSQL, MongoDB, Kafka)? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    docker-compose down
    echo "✓ Infrastructure stopped"
fi

echo "==================================="
echo "Shutdown complete"
echo "==================================="
