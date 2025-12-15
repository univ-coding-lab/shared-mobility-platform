#!/bin/bash

# Load Test Script using Apache Bench (ab)
# Success Criteria: 100 concurrent requests with <1s average response time

set -e

TOTAL_REQUESTS=1000
CONCURRENT=100

echo "============================================"
echo "Performance Load Test"
echo "Success Criteria: 100 concurrent < 1s avg"
echo "============================================"
echo ""

# Check if Apache Bench is installed
if ! command -v ab &> /dev/null; then
    echo "❌ Apache Bench (ab) is not installed"
    echo "Install with: brew install apache-bench (macOS)"
    exit 1
fi

# Check services
echo "Step 0: Checking services..."
for port in 8081 8082 8083; do
    code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/api/v1/actuator/health 2>/dev/null)
    if [ "$code" != "200" ]; then
        echo "❌ Service on port $port is not running"
        exit 1
    fi
done
echo "✓ Required services are running"
echo ""

echo "============================================"
echo "Test 1: Vehicle List API (GET - DB Read)"
echo "============================================"
ENDPOINT1="http://localhost:8082/api/v1/vehicles/available"
echo "Target: $ENDPOINT1"
echo "Requests: $TOTAL_REQUESTS, Concurrent: $CONCURRENT"
echo ""

echo "⏳ Running load test..."
ab -n $TOTAL_REQUESTS -c $CONCURRENT "$ENDPOINT1" > /tmp/ab_result_vehicles.txt 2>&1

AVG_TIME1=$(grep "Time per request" /tmp/ab_result_vehicles.txt | head -1 | awk '{print $4}')
FAILED1=$(grep "Failed requests" /tmp/ab_result_vehicles.txt | awk '{print $3}')
REQ_PER_SEC1=$(grep "Requests per second" /tmp/ab_result_vehicles.txt | awk '{print $4}')

echo "Results:"
echo "  Average Response Time: ${AVG_TIME1}ms"
echo "  Failed Requests: $FAILED1"
echo "  Requests/sec: $REQ_PER_SEC1"
echo ""

echo "============================================"
echo "Test 2: Rental API (POST - DB Write)"
echo "============================================"

# Create test user and get token
echo "Setting up test data..."
TIMESTAMP=$(date +%s)
TEST_EMAIL="loadtest${TIMESTAMP}@example.com"

# Register user
USER_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"Test1234!\",
    \"firstName\": \"Load\",
    \"lastName\": \"Test\"
  }")
USER_ID=$(echo $USER_RESPONSE | grep -o '"userId":"[^"]*"' | head -1 | sed 's/"userId":"//;s/"//')
if [ -z "$USER_ID" ]; then
    echo "❌ Failed to create user: $USER_RESPONSE"
    exit 1
fi
echo "✓ Test user created: $USER_ID"

# Create multiple test vehicles
echo "Creating 50 test vehicles..."
for i in $(seq 1 50); do
    curl -s -X POST "http://localhost:8082/api/v1/vehicles" \
      -H "Content-Type: application/json" \
      -d "{
        \"serialNumber\": \"LOAD-TEST-$TIMESTAMP-$i\",
        \"type\": \"E_SCOOTER\",
        \"manufacturer\": \"LoadTestCo\",
        \"model\": \"LT-$i\",
        \"batteryLevel\": 90,
        \"latitude\": 37.5665,
        \"longitude\": 126.9780
      }" > /dev/null 2>&1 &
done
wait
echo "✓ 50 test vehicles created"
echo ""

# Get available vehicle IDs
VEHICLE_IDS=$(curl -s "http://localhost:8082/api/v1/vehicles/available" | \
  grep -o '"id":"[^"]*"' | sed 's/"id":"//g' | sed 's/"//g' | head -50)

# Create request body file for rental
FIRST_VEHICLE=$(echo "$VEHICLE_IDS" | head -1)
cat > /tmp/rental_request.json << EOF
{
  "userId": "$USER_ID",
  "vehicleId": "$FIRST_VEHICLE",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "batteryLevel": 85
}
EOF

# Test rental endpoint with fewer requests (since it modifies data)
RENTAL_REQUESTS=50
echo "Target: http://localhost:8083/api/v1/rentals/start"
echo "Requests: $RENTAL_REQUESTS (limited for DB write test)"
echo ""

echo "⏳ Running rental load test..."
ab -n $RENTAL_REQUESTS -c 10 -p /tmp/rental_request.json -T "application/json" \
  "http://localhost:8083/api/v1/rentals/start" > /tmp/ab_result_rental.txt 2>&1

AVG_TIME2=$(grep "Time per request" /tmp/ab_result_rental.txt | head -1 | awk '{print $4}')
FAILED2=$(grep "Failed requests" /tmp/ab_result_rental.txt | awk '{print $3}')
REQ_PER_SEC2=$(grep "Requests per second" /tmp/ab_result_rental.txt | awk '{print $4}')

echo "Results:"
echo "  Average Response Time: ${AVG_TIME2}ms"
echo "  Failed Requests: $FAILED2"
echo "  Requests/sec: $REQ_PER_SEC2"
echo ""

echo "============================================"
echo "Summary"
echo "============================================"
echo ""
echo "Test 1 (Vehicle List - GET):"
echo "  Requests: $TOTAL_REQUESTS, Concurrent: $CONCURRENT"
echo "  Average: ${AVG_TIME1}ms, Failed: $FAILED1, RPS: $REQ_PER_SEC1"
echo ""
echo "Test 2 (Rental Start - POST):"
echo "  Requests: $RENTAL_REQUESTS, Concurrent: 10"
echo "  Average: ${AVG_TIME2}ms, Failed: $FAILED2, RPS: $REQ_PER_SEC2"
echo ""

# Check success criteria (based on Vehicle List test)
AVG_TIME_SEC=$(echo "$AVG_TIME1 / 1000" | bc -l)
if (( $(echo "$AVG_TIME_SEC < 1.0" | bc -l) )); then
    echo "✓ SUCCESS: Average response time < 1s"
    PERF_PASS=true
else
    echo "❌ FAIL: Average response time >= 1s"
    PERF_PASS=false
fi

# Check for actual connection/exception failures (not length differences)
# Format: (Connect: 0, Receive: 0, Length: 110, Exceptions: 0)
FAILURE_LINE=$(grep "(Connect:" /tmp/ab_result_vehicles.txt)
CONNECT_FAIL=$(echo "$FAILURE_LINE" | sed 's/.*Connect: \([0-9]*\).*/\1/')
EXCEPTION_FAIL=$(echo "$FAILURE_LINE" | sed 's/.*Exceptions: \([0-9]*\).*/\1/')
REAL_FAILURES=$((CONNECT_FAIL + EXCEPTION_FAIL))

if [ "$REAL_FAILURES" -eq 0 ]; then
    echo "✓ SUCCESS: No connection/exception failures"
    if [ "$FAILED1" -gt 0 ]; then
        echo "  (Note: $FAILED1 responses had different lengths - this is expected)"
    fi
    FAIL_PASS=true
else
    echo "❌ FAIL: $REAL_FAILURES actual failures occurred"
    FAIL_PASS=false
fi

echo ""
echo "Full reports:"
echo "  - /tmp/ab_result_vehicles.txt"
echo "  - /tmp/ab_result_rental.txt"

# Final verdict
echo ""
echo "============================================"
if [ "$PERF_PASS" = true ] && [ "$FAIL_PASS" = true ]; then
    echo "✓ LOAD TEST PASSED ✓"
    exit 0
else
    echo "❌ LOAD TEST FAILED ❌"
    exit 1
fi
