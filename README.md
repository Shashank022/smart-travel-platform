# Smart Travel Platform

A small microservices project that aggregates public travel APIs behind Spring Boot services.

## Phase 1 services

- `api-gateway` - single entry point on port `8080`
- `location-service` - geocodes a city using Open-Meteo Geocoding
- `weather-service` - current weather using Open-Meteo Forecast
- `currency-service` - currency conversion using Frankfurter
- `trip-planner-service` - orchestrates the other services into one travel summary

## Technology

- Java 21
- Spring Boot 4.0.7
- Spring Cloud 2025.1.2
- Spring Cloud Gateway WebFlux
- Spring WebFlux / WebClient
- Maven multi-module build

## Ports

| Service | Port |
|---|---:|
| API Gateway | 8080 |
| Trip Planner | 8081 |
| Location | 8082 |
| Weather | 8083 |
| Currency | 8084 |

## Run locally

Start the services in this order:

```bash
mvn -pl location-service spring-boot:run
mvn -pl weather-service spring-boot:run
mvn -pl currency-service spring-boot:run
mvn -pl trip-planner-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```


## Test

```bash
curl "http://localhost:8080/api/v1/travel/summary?city=Paris&fromCurrency=USD&toCurrency=EUR&budget=3000"
```

## Phase 2+

- PostgreSQL for saved trips
- Redis caching and rate limiting
- Kafka for `trip.created` events
- Resilience4j retries/circuit breakers
- Docker Compose
- OpenTelemetry + Prometheus/Grafana
- Kubernetes manifests
