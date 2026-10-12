#!/bin/bash

set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$PROJECT_DIR/logs"
PID_DIR="$PROJECT_DIR/.run"

mkdir -p "$LOG_DIR" "$PID_DIR"
cd "$PROJECT_DIR"

echo "Starting Redis and PostgreSQL..."
docker compose up -d redis postgres

echo "Waiting for Redis and PostgreSQL..."
ready=false
for _ in {1..60}; do
    if docker exec smart-travel-redis redis-cli ping 2>/dev/null | grep -q PONG \
        && docker exec smart-travel-postgres pg_isready -U smarttravel -d smarttravel >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done

if [ "$ready" != true ]; then
    echo "Redis or PostgreSQL did not become ready. Check: docker compose logs redis postgres" >&2
    exit 1
fi

wait_for_service() {
    local service_name="$1"
    local port="$2"

    echo "Waiting for $service_name on port $port..."
    for _ in {1..90}; do
        if curl -fsS "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo "$service_name is ready on port $port"
            return 0
        fi
        sleep 1
    done

    echo "$service_name failed to start. Check $LOG_DIR/$service_name.log" >&2
    return 1
}

start_service() {
    local service="$1"
    local port="$2"

    echo "Starting $service..."
    if curl -fsS "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
        echo "$service is already running on port $port"
        return 0
    fi

    nohup mvn -pl "$service" spring-boot:run \
        >"$LOG_DIR/$service.log" 2>&1 &
    echo "$!" >"$PID_DIR/$service.pid"
    wait_for_service "$service" "$port"
}

start_service location-service 8082
start_service weather-service 8083
start_service currency-service 8084
start_service trip-planner-service 8081
start_service api-gateway 8080

echo
echo "Smart Travel Platform is ready."
echo "Gateway: http://localhost:8080"
echo 'Test: curl "http://localhost:8080/api/v1/travel/summary?city=Paris&fromCurrency=USD&toCurrency=EUR&budget=3000"'
