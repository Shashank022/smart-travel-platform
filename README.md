# Smart Travel Platform

A Spring Boot microservices app that combines destination lookup, current weather, and currency conversion into a travel summary.

## Services

- API Gateway: `8080`
- Trip Planner: `8081`
- Location: `8082`
- Weather: `8083`
- Currency: `8084`
- Redis: `6379`
- PostgreSQL: `5432`

## Run with Docker Compose

Docker Compose builds and starts all five Spring services, Redis, and PostgreSQL:

```bash
docker compose up --build
```

Try the travel summary:

```bash
curl "http://localhost:8080/api/v1/travel/summary?city=Paris&fromCurrency=USD&toCurrency=EUR&budget=3000"
```

Saved trips are stored in PostgreSQL:

```bash
curl -X POST "http://localhost:8080/api/v1/trips" \
  -H "Content-Type: application/json" \
  -d '{"city":"Paris","fromCurrency":"USD","toCurrency":"EUR","budget":3000}'

curl "http://localhost:8080/api/v1/trips"
curl -X DELETE "http://localhost:8080/api/v1/trips/1"
```

To stop the containers while preserving saved trips, run `docker compose down`. To remove the database volume too, run `docker compose down -v`.

## Run locally with Maven

Use Java 21 and Maven. Start Redis and PostgreSQL in Docker, then launch the Spring services locally:

```bash
docker compose up -d redis postgres
./start-all.sh
```

The startup script waits for Redis and PostgreSQL before launching the services. Stop the services and local data containers with:

```bash
./stop-all.sh
```

## Caching

- Location lookups are cached in Redis for 24 hours.
- Weather results are cached for 15 minutes.
- Currency exchange rates are cached for 1 hour.

If Redis is unavailable, the services continue by calling their upstream APIs and skip cache writes.

## Circuit-breaker load test

Weather calls already use Resilience4j Retry and CircuitBreaker. The breaker opens when the weather provider has a high failure rate; request volume by itself does not open it. This opt-in endpoint generates predictable failures through the same retry and breaker instances without contacting Open-Meteo.

Start only Redis and the weather service with the local test endpoint enabled:

```bash
SMART_TRAVEL_RESILIENCE_TEST_ENABLED=true docker compose up --build redis weather-service
```

In TextPilot Studio, open Performance Lab and configure a **POST** request to `http://localhost:8083/api/v1/test/resilience/weather-failure`. Choose the **Burst** preset, set 100 requests and concurrency 10, then start the run. The endpoint intentionally returns HTTP 500; after enough failed calls, later requests are rejected by the open breaker.

Check the breaker state and events:

```bash
curl http://localhost:8083/actuator/circuitbreakers
curl http://localhost:8083/actuator/circuitbreakerevents
```

The endpoint is disabled by default. To reset the in-memory breaker for another run, restart the weather service with `docker compose restart weather-service`.

## Tech stack

Java 21, Spring Boot 4, Spring Cloud Gateway, Spring WebFlux, Redis, PostgreSQL, R2DBC, Flyway, and Resilience4j.

The credentials in `compose.yaml` are for local development only.
