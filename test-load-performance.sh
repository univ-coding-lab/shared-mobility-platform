#!/bin/bash

# Load Test Script using Apache Bench (ab)
# Success Criteria: 100 concurrent requests with <1s average response time

set -e

BASE_URL="http://localhost:8083"
TOTAL_REQUESTS=100
CONCURRENT=100

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "============================================"
echo "Performance Load Test"
echo "Success Criteria: 100 concurrent < 1s avg"
echo "============================================"
echo ""

# Check if Apache Bench is installed
if ! command -v ab &> /dev/null; then
    echo -e "${RED}❌ Apache Bench (ab) is not installed${NC}"
    echo "Install with: brew install apache-bench (macOS)"
    echo "Or use alternative: ./test-load-alternative.sh"
    exit 1
fi

# Test endpoint (health check for simplicity)
ENDPOINT="$BASE_URL/actuator/health"

echo "Test Configuration:"
echo "- Total Requests: $TOTAL_REQUESTS"
echo "- Concurrent Users: $CONCURRENT"
echo "- Target: $ENDPOINT"
echo ""

echo "⏳ Running load test..."
ab -n $TOTAL_REQUESTS -c $CONCURRENT "$ENDPOINT" > /tmp/ab_result.txt 2>&1

# Parse results
AVG_TIME=$(grep "Time per request" /tmp/ab_result.txt | head -1 | awk '{print $4}')
FAILED=$(grep "Failed requests" /tmp/ab_result.txt | awk '{print $3}')
REQ_PER_SEC=$(grep "Requests per second" /tmp/ab_result.txt | awk '{print $4}')

echo ""
echo "============================================"
echo "Load Test Results"
echo "============================================"
echo "Average Response Time: ${AVG_TIME}ms"
echo "Failed Requests: $FAILED"
echo "Requests/sec: $REQ_PER_SEC"
echo ""

# Check success criteria
AVG_TIME_SEC=$(echo "$AVG_TIME / 1000" | bc -l)
if (( $(echo "$AVG_TIME_SEC < 1.0" | bc -l) )); then
    echo -e "${GREEN}✓ SUCCESS: Average response time < 1s${NC}"
    PERF_PASS=true
else
    echo -e "${RED}❌ FAIL: Average response time >= 1s${NC}"
    PERF_PASS=false
fi

if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}✓ SUCCESS: No failed requests${NC}"
    FAIL_PASS=true
else
    echo -e "${YELLOW}⚠ WARNING: $FAILED requests failed${NC}"
    FAIL_PASS=false
fi

echo ""
echo "Full report saved to: /tmp/ab_result.txt"

# Final verdict
echo ""
echo "============================================"
if [ "$PERF_PASS" = true ] && [ "$FAIL_PASS" = true ]; then
    echo -e "${GREEN}✓ LOAD TEST PASSED ✓${NC}"
    exit 0
else
    echo -e "${RED}❌ LOAD TEST FAILED ❌${NC}"
    exit 1
fi
