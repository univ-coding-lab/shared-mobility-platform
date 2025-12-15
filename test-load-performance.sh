#!/bin/bash

# Load Test Script using Apache Bench (ab)
# Success Criteria: 100 concurrent requests with <1s average response time

set -e

BASE_URL="http://localhost:8083/api/v1"
TOTAL_REQUESTS=100
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
    echo "✓ SUCCESS: Average response time < 1s"
    PERF_PASS=true
else
    echo "❌ FAIL: Average response time >= 1s"
    PERF_PASS=false
fi

if [ "$FAILED" -eq 0 ]; then
    echo "✓ SUCCESS: No failed requests"
    FAIL_PASS=true
else
    echo "⚠ WARNING: $FAILED requests failed"
    FAIL_PASS=false
fi

echo ""
echo "Full report saved to: /tmp/ab_result.txt"

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
