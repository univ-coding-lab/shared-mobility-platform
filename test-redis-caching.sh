#!/bin/bash

# Redis Caching Effectiveness Verification Script

set -e

BASE_URL="http://localhost:8084"
VEHICLE_ID="VEH001"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================"
echo "Redis Caching Effectiveness Verification"
echo "========================================"
echo ""

# Step 1: Clear Redis cache
echo "Step 1: Clearing Redis cache..."
redis-cli FLUSHDB
echo -e "${GREEN}✓ Redis cache cleared${NC}"
echo ""

# Step 2: First location query (Cache MISS)
echo "Step 2: First query (Cache MISS - from MongoDB)..."
START_TIME=$(date +%s%N)
curl -s "$BASE_URL/locations/vehicle/$VEHICLE_ID/latest" > /dev/null
END_TIME=$(date +%s%N)
MISS_TIME=$(( ($END_TIME - $START_TIME) / 1000000 ))
echo -e "${YELLOW}Cache MISS: ${MISS_TIME}ms${NC}"
echo ""

# Step 3: Second query (Cache HIT)
echo "Step 3: Second query (Cache HIT - from Redis)..."
START_TIME=$(date +%s%N)
curl -s "$BASE_URL/locations/vehicle/$VEHICLE_ID/latest" > /dev/null
END_TIME=$(date +%s%N)
HIT_TIME=$(( ($END_TIME - $START_TIME) / 1000000 ))
echo -e "${GREEN}Cache HIT: ${HIT_TIME}ms${NC}"
echo ""

# Step 4: Calculate improvement
if [ $MISS_TIME -gt 0 ]; then
    IMPROVEMENT=$(( ($MISS_TIME - $HIT_TIME) * 100 / $MISS_TIME ))
    echo "========================================="
    echo "Caching Performance Improvement"
    echo "========================================="
    echo "Cache MISS: ${MISS_TIME}ms"
    echo "Cache HIT:  ${HIT_TIME}ms"
    echo -e "${GREEN}Improvement: ${IMPROVEMENT}%${NC}"
    echo ""

    if [ $IMPROVEMENT -ge 50 ]; then
        echo -e "${GREEN}✓ Redis caching is highly effective (>50% improvement)${NC}"
    else
        echo -e "${YELLOW}⚠ Caching improvement is moderate (<50%)${NC}"
    fi
else
    echo -e "${YELLOW}⚠ Could not measure performance${NC}"
fi

echo ""
echo "💡 Presentation claim: 10x faster (3s → 0.3s)"
echo "💡 Actual improvement: ${IMPROVEMENT}% faster"
