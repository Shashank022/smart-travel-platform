#!/bin/bash

set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$PROJECT_DIR/.run"

stop_service() {
    local service="$1"
    local port="$2"
    local pid_file="$PID_DIR/$service.pid"

    echo "Stopping $service..."
    if [ -f "$pid_file" ]; then
        local pid
        pid="$(cat "$pid_file")"
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null || true
        fi
        rm -f "$pid_file"
    fi

    local port_pids
    port_pids="$(lsof -ti "tcp:$port" 2>/dev/null || true)"
    if [ -n "$port_pids" ]; then
        kill $port_pids 2>/dev/null || true
    fi
}

stop_service api-gateway 8080
stop_service trip-planner-service 8081
stop_service location-service 8082
stop_service weather-service 8083
stop_service currency-service 8084

cd "$PROJECT_DIR"
docker compose stop redis postgres >/dev/null 2>&1 || true

echo "Smart Travel Platform services stopped. PostgreSQL data is preserved."
