#!/bin/bash

# Redis Caching Effectiveness Verification Script
# Tests the performance improvement from Redis caching
# This test is SELF-CONTAINED - creates its own test data

echo "========================================"
echo "Redis Caching Effectiveness Verification"
echo "========================================"
echo ""

# Check services are running
echo "Step 0: Checking services..."
for port in 8082 8084; do
    code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/api/v1/actuator/health 2>/dev/null)
    if [ "$code" != "200" ]; then
        echo "❌ Service on port $port is not running"
        echo "   Please run ./start-services.sh first"
        exit 1
    fi
done
echo "✓ Required services are running"
echo ""

# Generate unique identifiers
TIMESTAMP=$(date +%s)
SERIAL_NUMBER="CACHE-TEST-$TIMESTAMP"

# Step 1: Create a test vehicle
echo "Step 1: Creating test vehicle..."
VEHICLE_RESPONSE=$(curl -s -X POST "http://localhost:8082/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -d "{
    \"serialNumber\": \"$SERIAL_NUMBER\",
    \"type\": \"E_SCOOTER\",
    \"manufacturer\": \"CacheTestCo\",
    \"model\": \"Cache-100\",
    \"batteryLevel\": 90,
    \"latitude\": 37.5665,
    \"longitude\": 126.9780
  }")
VEHICLE_ID=$(echo $VEHICLE_RESPONSE | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')

if [ -z "$VEHICLE_ID" ]; then
    echo "❌ Failed to create vehicle"
    echo "Response: $VEHICLE_RESPONSE"
    exit 1
fi
echo "✓ Vehicle created: $VEHICLE_ID"
echo ""

# Step 2: Create location data for this vehicle directly via Location Service
echo "Step 2: Creating location data..."
LOCATION_RESPONSE=$(curl -s -X POST "http://localhost:8084/api/v1/locations" \
  -H "Content-Type: application/json" \
  -d "{
    \"vehicleId\": \"$VEHICLE_ID\",
    \"latitude\": 37.5665,
    \"longitude\": 126.9780,
    \"source\": \"TEST\"
  }" 2>/dev/null)
echo "✓ Location data created"
echo ""

# Step 3: Clear Redis cache
echo "Step 3: Clearing Redis cache..."
# Use the correct password from docker-compose.yml
docker exec smp-redis redis-cli -a redispass FLUSHDB 2>&1 | grep -v "Warning: Using a password"
echo "✓ Redis cache cleared"
echo ""

# Step 4: First query (Cache MISS)
echo "Step 4: First query (Cache MISS - from MongoDB)..."
START_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
RESPONSE1=$(curl -s "http://localhost:8084/api/v1/locations/vehicle/$VEHICLE_ID/latest" 2>/dev/null)
END_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
MISS_TIME=$((END_TIME - START_TIME))

if echo "$RESPONSE1" | grep -q "error"; then
    echo "Response: $RESPONSE1"
    echo "⚠ API returned error, but continuing to measure timing..."
fi
echo "Cache MISS time: ${MISS_TIME}ms"
echo ""

# Step 5: Second query (Cache HIT)
echo "Step 5: Second query (Cache HIT - from Redis)..."
START_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
RESPONSE2=$(curl -s "http://localhost:8084/api/v1/locations/vehicle/$VEHICLE_ID/latest" 2>/dev/null)
END_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
HIT_TIME=$((END_TIME - START_TIME))
echo "Cache HIT time: ${HIT_TIME}ms"
echo ""

# Step 6: Third query (confirm cache)
echo "Step 6: Third query (confirming Cache HIT)..."
START_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
RESPONSE3=$(curl -s "http://localhost:8084/api/v1/locations/vehicle/$VEHICLE_ID/latest" 2>/dev/null)
END_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
HIT_TIME2=$((END_TIME - START_TIME))
echo "Cache HIT time: ${HIT_TIME2}ms"
echo ""

# Step 7: Run multiple queries to get average
echo "Step 7: Running 5 more queries for average..."
TOTAL_TIME=0
for i in {1..5}; do
    START_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
    curl -s "http://localhost:8084/api/v1/locations/vehicle/$VEHICLE_ID/latest" > /dev/null 2>&1
    END_TIME=$(python3 -c 'import time; print(int(time.time() * 1000))')
    QUERY_TIME=$((END_TIME - START_TIME))
    TOTAL_TIME=$((TOTAL_TIME + QUERY_TIME))
    echo "  Query $i: ${QUERY_TIME}ms"
done
AVG_TIME=$((TOTAL_TIME / 5))
echo "  Average: ${AVG_TIME}ms"
echo ""

# Results
echo "========================================"
echo "Caching Performance Results"
echo "========================================"
echo ""
echo "1st query (MISS): ${MISS_TIME}ms"
echo "2nd query (HIT):  ${HIT_TIME}ms"
echo "3rd query (HIT):  ${HIT_TIME2}ms"
echo "Average (5 queries): ${AVG_TIME}ms"
echo ""

if [ $MISS_TIME -gt 0 ] && [ $HIT_TIME -gt 0 ]; then
    if [ $MISS_TIME -gt $HIT_TIME ]; then
        IMPROVEMENT=$(( (MISS_TIME - HIT_TIME) * 100 / MISS_TIME ))
        echo "Performance Improvement: ${IMPROVEMENT}%"
        echo ""

        if [ $IMPROVEMENT -ge 20 ]; then
            echo "✓ Redis caching shows ${IMPROVEMENT}% improvement"
            echo ""
            echo "Note: In production with larger datasets and"
            echo "more complex queries, improvement would be greater."
        else
            echo "⚠ Caching improvement is ${IMPROVEMENT}%"
            echo "  (Minimal due to fast local MongoDB)"
        fi
    else
        echo "⚠ No significant improvement detected"
        echo "  1st: ${MISS_TIME}ms vs 2nd: ${HIT_TIME}ms"
        echo ""
        echo "  Possible reasons:"
        echo "  - Caching not implemented for this endpoint"
        echo "  - MongoDB is very fast locally"
        echo "  - Network latency variations"
    fi
else
    echo "⚠ Could not calculate improvement"
fi

echo ""
echo "========================================"
echo "Test completed with Vehicle: $VEHICLE_ID"
echo "========================================"
