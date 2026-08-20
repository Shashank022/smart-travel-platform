#!/bin/bash

set -e

PROJECT_DIR="/Users/shashank/Documents/GitHub/smart-travel-platform"
LOG_DIR="$PROJECT_DIR/logs"
PID_DIR="$PROJECT_DIR/.run"

mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

cd "$PROJECT_DIR"

echo "=========================================="
echo " Starting Smart Travel Platform"
echo "=========================================="

# ------------------------------------------------
# Redis
# ------------------------------------------------

echo ""
echo "Checking Redis..."

if docker ps --format '{{.Names}}' | grep -q '^smart-travel-redis$'; then
    echo "✅ Redis already running"
elif docker ps -a --format '{{.Names}}' | grep -q '^smart-travel-redis$'; then
    echo "Starting existing Redis container..."
    docker start smart-travel-redis >/dev/null
    echo "✅ Redis started"
else
    echo "Creating Redis container..."
    docker run -d \
        --name smart-travel-redis \
        -p 6379:6379 \
        redis:7-alpine >/dev/null

    echo "✅ Redis created and started"
fi

# Verify Redis
until docker exec smart-travel-redis redis-cli ping 2>/dev/null | grep -q PONG
do
    echo "Waiting for Redis..."
    sleep 1
done

echo "✅ Redis responding on port 6379"


# ------------------------------------------------
# Function to wait for Spring Boot service
# ------------------------------------------------

wait_for_service() {

    SERVICE_NAME=$1
    PORT=$2

    echo "Waiting for $SERVICE_NAME on port $PORT..."

    for i in {1..60}
    do
        if curl -fsS "http://localhost:$PORT/actuator/health" >/dev/null 2>&1
        then
            echo "✅ $SERVICE_NAME started on port $PORT"
            return
        fi

        sleep 1
    done

    echo "❌ $SERVICE_NAME failed to start"
    echo "Check log:"
    echo "$LOG_DIR/$SERVICE_NAME.log"
    exit 1
}


# ------------------------------------------------
# Function to start Maven service
# ------------------------------------------------

start_service() {

    SERVICE=$1
    PORT=$2

    echo ""
    echo "------------------------------------------"
    echo "Starting $SERVICE"
    echo "------------------------------------------"

    # Check if already running
    if curl -fsS "http://localhost:$PORT/actuator/health" >/dev/null 2>&1
    then
        echo "✅ $SERVICE already running on port $PORT"
        return
    fi

    nohup mvn -pl "$SERVICE" spring-boot:run \
        > "$LOG_DIR/$SERVICE.log" 2>&1 &

    PID=$!

    echo "$PID" > "$PID_DIR/$SERVICE.pid"

    echo "PID: $PID"

    wait_for_service "$SERVICE" "$PORT"
}


# ------------------------------------------------
# Start downstream services first
# ------------------------------------------------

start_service "location-service" 8082
start_service "weather-service" 8083
start_service "currency-service" 8084

# ------------------------------------------------
# Start aggregator
# ------------------------------------------------

start_service "trip-planner-service" 8081

# ------------------------------------------------
# Start API Gateway LAST
# ------------------------------------------------

start_service "api-gateway" 8080


echo ""
echo "=========================================="
echo " 🎉 Smart Travel Platform is READY"
echo "=========================================="

echo ""
echo "Services:"
echo ""
echo "API Gateway        http://localhost:8080"
echo "Trip Planner       http://localhost:8081"
echo "Location Service   http://localhost:8082"
echo "Weather Service    http://localhost:8083"
echo "Currency Service   http://localhost:8084"
echo "Redis              localhost:6379"

echo ""
echo "Health checks:"
echo ""

echo "Gateway:"
curl -s http://localhost:8080/actuator/health
echo ""

echo "Trip Planner:"
curl -s http://localhost:8081/actuator/health
echo ""

echo "Location:"
curl -s http://localhost:8082/actuator/health
echo ""

echo "Weather:"
curl -s http://localhost:8083/actuator/health
echo ""

echo "Currency:"
curl -s http://localhost:8084/actuator/health
echo ""

echo ""
echo "=========================================="
echo "Test complete travel API:"
echo ""
echo 'curl "http://localhost:8080/api/v1/travel/summary?city=Paris&fromCurrency=USD&toCurrency=EUR&budget=3000"'
echo "=========================================="