#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$PROJECT_DIR/.run"

echo "=========================================="
echo " Stopping Smart Travel Platform"
echo "=========================================="

stop_service() {
    SERVICE=$1
    PORT=$2
    PID_FILE="$PID_DIR/$SERVICE.pid"

    echo ""
    echo "Stopping $SERVICE..."

    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")

        if kill -0 "$PID" 2>/dev/null; then
            kill "$PID"
            echo "✅ $SERVICE stopped (PID $PID)"
        else
            echo "ℹ️ $SERVICE already stopped"
        fi

        rm -f "$PID_FILE"
    else
        PID=$(lsof -ti tcp:$PORT 2>/dev/null)

        if [ -n "$PID" ]; then
            kill $PID
            echo "✅ $SERVICE stopped from port $PORT"
        else
            echo "ℹ️ $SERVICE is not running"
        fi
    fi
}

# Stop in reverse dependency order
stop_service "api-gateway" 8080
stop_service "trip-planner-service" 8081
stop_service "location-service" 8082
stop_service "weather-service" 8083
stop_service "currency-service" 8084

echo ""
#echo "Stopping Redis..."
#
#if docker ps --format '{{.Names}}' | grep -q '^smart-travel-redis$'; then
#    docker stop smart-travel-redis >/dev/null
#    echo "✅ Redis stopped"
#else
#    echo "ℹ️ Redis already stopped"
#fi

echo ""
echo "=========================================="
echo " ✅ Smart Travel Platform stopped"
echo "=========================================="
