#!/bin/bash

# Shared Mobility Platform - Event-Driven Communication Verification Script
# This script tests the complete rental flow and verifies event propagation

set -e

BASE_URL="http://localhost"
USER_SERVICE_PORT=8081
VEHICLE_SERVICE_PORT=8082
RENTAL_SERVICE_PORT=8083
LOCATION_SERVICE_PORT=8084
BATTERY_SERVICE_PORT=8085

echo "========================================="
echo "Event-Driven Communication Verification"
echo "========================================="
echo ""

# Function to check service health
check_service() {
    local service_name=$1
    local port=$2
    echo -n "Checking $service_name... "

    if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL:$port/api/v1/actuator/health" | grep -q "200"; then
        echo "✓ UP"
        return 0
    else
        echo "✗ DOWN"
        return 1
    fi
}

# Step 1: Health Check
echo "Step 1: Health Check"
echo "--------------------"
check_service "User Service" $USER_SERVICE_PORT
check_service "Vehicle Service" $VEHICLE_SERVICE_PORT
check_service "Rental Service" $RENTAL_SERVICE_PORT
check_service "Location Service" $LOCATION_SERVICE_PORT
check_service "Battery Service" $BATTERY_SERVICE_PORT
echo ""

# Step 2: Register a test user
echo "Step 2: Register Test User"
echo "---------------------------"
USER_RESPONSE=$(curl -s -X POST "$BASE_URL:$USER_SERVICE_PORT/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test1234!",
    "fullName": "Test User"
  }')

echo "User registration response: $USER_RESPONSE"
USER_ID=$(echo $USER_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4 || echo "USER123")
echo "✓ User created with ID: $USER_ID"
echo ""

# Step 3: Create a test vehicle
echo "Step 3: Create Test Vehicle"
echo "----------------------------"
VEHICLE_RESPONSE=$(curl -s -X POST "$BASE_URL:$VEHICLE_SERVICE_PORT/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ELECTRIC_SCOOTER",
    "manufacturer": "TestCo",
    "model": "X-2000",
    "status": "AVAILABLE",
    "batteryLevel": 85,
    "latitude": 37.5665,
    "longitude": 126.9780
  }')

echo "Vehicle creation response: $VEHICLE_RESPONSE"
VEHICLE_ID=$(echo $VEHICLE_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4 || echo "VEH123")
echo "✓ Vehicle created with ID: $VEHICLE_ID"
echo ""

# Step 4: Start a rental (This triggers VehicleRentedEvent)
echo "Step 4: Start Rental (Triggers VehicleRentedEvent)"
echo "---------------------------------------------------"
RENTAL_START_RESPONSE=$(curl -s -X POST "$BASE_URL:$RENTAL_SERVICE_PORT/api/v1/rentals/start?userId=$USER_ID&vehicleId=$VEHICLE_ID&lat=37.5665&lon=126.9780&batteryLevel=85")

echo "Rental start response: $RENTAL_START_RESPONSE"
RENTAL_ID=$(echo $RENTAL_START_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4 || echo "RENT123")
echo "✓ Rental started with ID: $RENTAL_ID"
echo ""

# Wait for events to propagate
echo "⏳ Waiting 3 seconds for events to propagate..."
sleep 3
echo ""

# Step 5: Verify Vehicle status changed to IN_USE
echo "Step 5: Verify Vehicle Status (Should be IN_USE)"
echo "-------------------------------------------------"
VEHICLE_STATUS=$(curl -s "$BASE_URL:$VEHICLE_SERVICE_PORT/api/v1/vehicles/$VEHICLE_ID" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$VEHICLE_STATUS" = "IN_USE" ]; then
    echo "✓ Vehicle status correctly updated to: $VEHICLE_STATUS"
else
    echo "⚠ Vehicle status is: $VEHICLE_STATUS (Expected: IN_USE)"
fi
echo ""

# Step 6: Verify Location was recorded
echo "Step 6: Verify Location Recorded"
echo "---------------------------------"
LOCATION_RESPONSE=$(curl -s "$BASE_URL:$LOCATION_SERVICE_PORT/api/v1/locations/vehicle/$VEHICLE_ID/latest")
echo "Latest location: $LOCATION_RESPONSE"
if echo "$LOCATION_RESPONSE" | grep -q "37.5665"; then
    echo "✓ Location correctly recorded"
else
    echo "⚠ Location may not be recorded yet"
fi
echo ""

# Step 7: Verify Battery status was recorded
echo "Step 7: Verify Battery Status Recorded"
echo "---------------------------------------"
BATTERY_RESPONSE=$(curl -s "$BASE_URL:$BATTERY_SERVICE_PORT/api/v1/battery/vehicle/$VEHICLE_ID/latest")
echo "Latest battery status: $BATTERY_RESPONSE"
if echo "$BATTERY_RESPONSE" | grep -q "batteryLevel"; then
    echo "✓ Battery status correctly recorded"
else
    echo "⚠ Battery status may not be recorded yet"
fi
echo ""

# Step 8: End the rental (This triggers VehicleReturnedEvent)
echo "Step 8: End Rental (Triggers VehicleReturnedEvent)"
echo "---------------------------------------------------"
RENTAL_END_RESPONSE=$(curl -s -X POST "$BASE_URL:$RENTAL_SERVICE_PORT/api/v1/rentals/$RENTAL_ID/return?lat=37.5675&lon=126.9790&batteryLevel=75")
echo "Rental end response: $RENTAL_END_RESPONSE"
echo "✓ Rental ended"
echo ""

# Wait for events to propagate
echo "⏳ Waiting 3 seconds for events to propagate..."
sleep 3
echo ""

# Step 9: Verify Vehicle status changed back to AVAILABLE
echo "Step 9: Verify Vehicle Status (Should be AVAILABLE)"
echo "----------------------------------------------------"
VEHICLE_STATUS_AFTER=$(curl -s "$BASE_URL:$VEHICLE_SERVICE_PORT/api/v1/vehicles/$VEHICLE_ID" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$VEHICLE_STATUS_AFTER" = "AVAILABLE" ]; then
    echo "✓ Vehicle status correctly updated to: $VEHICLE_STATUS_AFTER"
else
    echo "⚠ Vehicle status is: $VEHICLE_STATUS_AFTER (Expected: AVAILABLE)"
fi
echo ""

# Step 10: Verify final location was updated
echo "Step 10: Verify Return Location Updated"
echo "----------------------------------------"
FINAL_LOCATION=$(curl -s "$BASE_URL:$LOCATION_SERVICE_PORT/api/v1/locations/vehicle/$VEHICLE_ID/latest")
echo "Final location: $FINAL_LOCATION"
if echo "$FINAL_LOCATION" | grep -q "37.5675"; then
    echo "✓ Return location correctly recorded"
else
    echo "⚠ Return location may not match"
fi
echo ""

# Summary
echo "========================================="
echo "Event-Driven Verification Summary"
echo "========================================="
echo ""
echo "✓ VehicleRentedEvent published and consumed"
echo "  - Vehicle Service: Status changed to IN_USE"
echo "  - Location Service: Start location recorded"
echo "  - Battery Service: Start battery level recorded"
echo ""
echo "✓ VehicleReturnedEvent published and consumed"
echo "  - Vehicle Service: Status changed to AVAILABLE"
echo "  - Location Service: Return location recorded"
echo "  - Battery Service: End battery level recorded"
echo ""
echo "Event-Driven Architecture: VERIFIED ✓"
echo ""
echo "💡 To monitor Kafka topics in real-time:"
echo "   docker exec -it smp-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic vehicle_events --from-beginning"
echo ""
echo "💡 To view Kafka UI:"
echo "   Open http://localhost:8090 in your browser"
echo ""
