# Grafana Dashboards

This directory contains Grafana dashboard definitions for the Shared Mobility Platform.

## Quick Setup

### Option 1: Import Pre-built Dashboards (Recommended)

After starting Grafana (http://localhost:3000), manually import these community dashboards:

1. **JVM (Micrometer)** - Dashboard ID: `4701`
   - Provides JVM metrics (heap, GC, threads)
   - Works with Spring Boot Actuator + Prometheus

2. **Spring Boot Statistics** - Dashboard ID: `6756`
   - HTTP request rates, response times, error rates
   - Database connection pool metrics

3. **Kafka Overview** - Dashboard ID: `7589`
   - Topic throughput, consumer lag
   - Broker metrics

### Option 2: Create Custom Dashboards

Access Grafana at http://localhost:3000 (admin/admin) and create dashboards with these panels:

## Dashboard 1: Services Overview

### Panels:

1. **Service Status**
   ```promql
   up{job=~".*-service|api-gateway|iot-simulator"}
   ```

2. **Request Rate (RPS)**
   ```promql
   sum(rate(http_server_requests_seconds_count[5m])) by (job)
   ```

3. **Average Response Time**
   ```promql
   histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (job, le))
   ```

4. **Error Rate**
   ```promql
   sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (job)
   ```

5. **Memory Usage**
   ```promql
   jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
   ```

## Dashboard 2: Kafka Metrics

### Panels:

1. **Messages Published (by Topic)**
   ```promql
   sum(rate(kafka_producer_record_send_total[5m])) by (topic)
   ```

2. **Messages Consumed (by Consumer Group)**
   ```promql
   sum(rate(kafka_consumer_fetch_manager_records_consumed_total[5m])) by (client_id)
   ```

3. **Consumer Lag**
   ```promql
   kafka_consumer_fetch_manager_records_lag_max
   ```

## Dashboard 3: Database Metrics

### Panels:

1. **Active Connections**
   ```promql
   hikaricp_connections_active{pool=~".*"}
   ```

2. **Connection Pool Usage**
   ```promql
   hikaricp_connections_active / hikaricp_connections_max
   ```

3. **Query Duration**
   ```promql
   rate(hikaricp_connections_usage_seconds_sum[5m]) / rate(hikaricp_connections_usage_seconds_count[5m])
   ```

## Dashboard 4: IoT Simulator

### Panels:

1. **Simulated Vehicles Count**
   ```promql
   count(up{job="iot-simulator"})
   ```

2. **Vehicle Events Published**
   ```promql
   sum(rate(kafka_producer_record_send_total{job="iot-simulator", topic="vehicle.moved"}[5m]))
   ```

## Accessing Grafana

1. Start services: `docker-compose up -d`
2. Access Grafana: http://localhost:3000
3. Login: admin / admin
4. Datasource "Prometheus" is auto-configured
5. Import dashboards using IDs above or create custom ones

## Useful PromQL Queries

### Service Health
- Service uptime: `up{job="vehicle-service"}`
- Service restart count: `process_uptime_seconds{job="vehicle-service"}`

### Performance
- Request throughput: `rate(http_server_requests_seconds_count[1m])`
- P95 latency: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
- Error percentage: `(sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))) * 100`

### Resource Usage
- Heap usage: `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100`
- GC rate: `rate(jvm_gc_pause_seconds_count[5m])`
- Thread count: `jvm_threads_live`

### Business Metrics
- Rentals started: `sum(increase(http_server_requests_seconds_count{uri="/api/v1/rentals/start"}[1h]))`
- Battery low events: `sum(increase(kafka_producer_record_send_total{topic="battery.low"}[1h]))`
