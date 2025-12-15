# Shell Scripts

## 사전 요구사항

- Docker 실행 중 (`docker-compose up -d`)
- 빌드 완료 (`./gradlew clean build -x test`)

---

## 서비스 관리

### start-services.sh

5개 마이크로서비스를 백그라운드로 시작합니다.

```bash
./start-services.sh
```

시작되는 서비스:
- User Service (8081)
- Vehicle Service (8082)
- Rental Service (8083)
- Location Service (8084)
- Battery Service (8085)

### stop-services.sh

실행 중인 모든 서비스를 종료합니다.

```bash
./stop-services.sh
```

---

## 테스트 스크립트

### test-event-driven.sh

Kafka 이벤트 기반 통신을 검증합니다.

```bash
./test-event-driven.sh
```

검증 내용:
- 유저 등록 → 차량 등록 → 대여 시작 → 대여 종료 흐름 실행
- VehicleRentedEvent, VehicleReturnedEvent 발행 확인
- Vehicle, Location, Battery 서비스의 이벤트 수신 확인

### test-fault-isolation.sh

장애 격리를 테스트합니다.

```bash
./test-fault-isolation.sh
```

검증 내용:
- Battery Service 강제 종료 (pkill)
- 다른 4개 서비스 정상 동작 확인
- Battery Service 자동 재시작

### test-redis-caching.sh

Redis 캐싱 효과를 측정합니다.

```bash
./test-redis-caching.sh
```

검증 내용:
- 테스트 차량/위치 데이터 자동 생성
- Cache MISS (MongoDB) vs Cache HIT (Redis) 응답 시간 비교
- 성능 개선율 출력

### test-load-performance.sh

Apache Bench를 사용한 부하 테스트입니다.

```bash
./test-load-performance.sh
```

검증 내용:
- Test 1: 차량 목록 API (GET) - 1000건, 동시 100
- Test 2: 대여 시작 API (POST) - 50건, 동시 10
- 성공 기준: 평균 응답 시간 < 1초
