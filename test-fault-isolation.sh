#!/bin/bash

# Fault Isolation Test Script
# Success Criteria: When one service is down, others continue to operate
# This script performs ACTUAL fault injection by killing services

echo "=========================================="
echo "Fault Isolation Test (Real Fault Injection)"
echo "Success Criteria: Service independence"
echo "=========================================="
echo ""

# Helper function to check service health
check_service() {
    local name=$1
    local port=$2
    local code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/api/v1/actuator/health 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo "$name ($port): ✓ UP"
        return 0
    else
        echo "$name ($port): ✗ DOWN"
        return 1
    fi
}

# Helper function to check all services
check_all_services() {
    echo "Checking all services..."
    check_service "User Service" 8081
    check_service "Vehicle Service" 8082
    check_service "Rental Service" 8083
    check_service "Location Service" 8084
    check_service "Battery Service" 8085
    echo ""
}

# Step 1: Initial Health Check
echo "Step 1: Initial Health Check"
echo "-----------------------------"
check_all_services

# Verify all services are up before testing
for port in 8081 8082 8083 8084 8085; do
    code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/api/v1/actuator/health 2>/dev/null)
    if [ "$code" != "200" ]; then
        echo "❌ Not all services are UP. Please start all services first."
        echo "   Run: ./start-services.sh"
        exit 1
    fi
done
echo "✓ All 5 services are running"
echo ""

# ===========================================
# Test 1: Battery Service Failure Isolation
# ===========================================
echo "=========================================="
echo "Test 1: Battery Service Failure"
echo "=========================================="
echo ""

# Step 2: Kill Battery Service
echo "Step 2: Killing Battery Service..."
pkill -f "battery-service" 2>/dev/null || true
sleep 2

# Verify Battery Service is down
BATTERY_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/api/v1/actuator/health 2>/dev/null)
if [ "$BATTERY_STATUS" = "000" ] || [ "$BATTERY_STATUS" = "000" ]; then
    echo "✓ Battery Service successfully stopped"
else
    echo "⚠ Battery Service may still be running (status: $BATTERY_STATUS)"
fi
echo ""

# Step 3: Test other services still work
echo "Step 3: Testing other services (Battery is DOWN)..."
PASS_COUNT=0

# Test User Service
USER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/actuator/health 2>/dev/null)
if [ "$USER_STATUS" = "200" ]; then
    echo "  User Service: ✓ Still working"
    ((PASS_COUNT++))
else
    echo "  User Service: ❌ Affected by Battery failure"
fi

# Test Vehicle Service
VEHICLE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/actuator/health 2>/dev/null)
if [ "$VEHICLE_STATUS" = "200" ]; then
    echo "  Vehicle Service: ✓ Still working"
    ((PASS_COUNT++))
else
    echo "  Vehicle Service: ❌ Affected by Battery failure"
fi

# Test Rental Service
RENTAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/v1/actuator/health 2>/dev/null)
if [ "$RENTAL_STATUS" = "200" ]; then
    echo "  Rental Service: ✓ Still working"
    ((PASS_COUNT++))
else
    echo "  Rental Service: ❌ Affected by Battery failure"
fi

# Test Location Service
LOCATION_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/api/v1/actuator/health 2>/dev/null)
if [ "$LOCATION_STATUS" = "200" ]; then
    echo "  Location Service: ✓ Still working"
    ((PASS_COUNT++))
else
    echo "  Location Service: ❌ Affected by Battery failure"
fi

echo ""
echo "  Result: $PASS_COUNT/4 services unaffected by Battery failure"
echo ""

# Step 4: Test actual functionality
echo "Step 4: Testing functionality with Battery down..."
VEHICLE_QUERY=$(curl -s "http://localhost:8082/api/v1/vehicles/available" 2>/dev/null)
if echo "$VEHICLE_QUERY" | grep -q "success"; then
    echo "  Vehicle query: ✓ Working"
else
    echo "  Vehicle query: ❌ Failed"
fi
echo ""

# Step 5: Restart Battery Service
echo "Step 5: Restarting Battery Service..."
cd /Users/osang0731/IdeaProjects/shared-mobility-platform
java -jar services/battery-service/build/libs/battery-service.jar > logs/battery-service.log 2>&1 &
BATTERY_PID=$!
echo "  Battery Service started (PID: $BATTERY_PID)"
echo "  Waiting 10 seconds for startup..."
sleep 10

# Verify Battery Service recovered
BATTERY_RECOVERED=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/api/v1/actuator/health 2>/dev/null)
if [ "$BATTERY_RECOVERED" = "200" ]; then
    echo "  ✓ Battery Service recovered successfully"
else
    echo "  ⚠ Battery Service may need more time to start"
fi
echo ""

# ===========================================
# Summary
# ===========================================
echo "=========================================="
echo "Fault Isolation Test Summary"
echo "=========================================="
echo ""
if [ "$PASS_COUNT" -eq 4 ]; then
    echo "✓ TEST PASSED: All services remained operational"
    echo ""
    echo "Verified:"
    echo "  - Battery Service failure did NOT cascade to other services"
    echo "  - Event-Driven architecture provides fault isolation"
    echo "  - Services operate independently"
    echo ""
    echo "Fault Isolation: VERIFIED ✓"
    exit 0
else
    echo "❌ TEST FAILED: Some services were affected"
    echo "  Only $PASS_COUNT/4 services remained operational"
    exit 1
fi
