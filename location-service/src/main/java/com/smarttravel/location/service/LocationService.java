package com.smarttravel.location.service;

import com.smarttravel.location.dto.LocationResponse;
import com.smarttravel.location.dto.OpenMeteoGeocodingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Locale;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;

@Service
public class LocationService {

    private static final Logger log =
            LoggerFactory.getLogger(LocationService.class);

    private static final Duration LOCATION_CACHE_TTL = Duration.ofHours(24);

    private final WebClient webClient;

    private final ReactiveRedisTemplate<String, LocationResponse> redisTemplate;

    private final CircuitBreaker locationCircuitBreaker;
    private final Retry locationRetry;

    public LocationService(
            WebClient.Builder builder,
            ReactiveRedisTemplate<String, LocationResponse> redisTemplate,
            @Value("${clients.open-meteo-geocoding.base-url}")
                    String baseUrl, CircuitBreakerRegistry circuitBreakerRegistry,RetryRegistry retryRegistry) {

        this.webClient = builder.baseUrl(baseUrl).build();
        this.redisTemplate = redisTemplate;
        this.locationCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("locationApi");

        this.locationRetry =
                retryRegistry.retry("locationApi");
    }

    public Mono<LocationResponse> findByCity(String city) {

        String cacheKey =  "location:" + city.trim().toLowerCase(Locale.ROOT);

        return redisTemplate
                .opsForValue()
                .get(cacheKey)

                // Redis contains the value
                .doOnNext(value ->
                        log.info("Redis HIT: {}", cacheKey))

                // Redis does not contain the value
                .onErrorResume(exception -> {
                    log.warn(
                            "Redis read failed for {}. Calling Open-Meteo api..",
                            cacheKey
                    );

                    return Mono.empty();
                })

                .switchIfEmpty(
                        Mono.defer(() -> {

                            log.info("Redis MISS: {}",  cacheKey);

                            return fetchFromOpenMeteo(city)
                                    .flatMap(location ->
                                            redisTemplate
                                                    .opsForValue()
                                                    .set(
                                                            cacheKey,
                                                            location,
                                                            LOCATION_CACHE_TTL
                                                    )

                                                    .doOnNext(saved ->
                                                            log.info(
                                                                    "Stored {} in Redis with TTL {}",
                                                                    cacheKey,
                                                                    LOCATION_CACHE_TTL
                                                            )
                                                    )

                                                    .thenReturn(location)

                                                    // Redis failure should not
                                                    // break location API
                                                    .onErrorResume(exception -> {

                                                        log.warn(
                                                                "Unable to cache {}",
                                                                cacheKey
                                                        );

                                                        return Mono.just(location);
                                                    })
                                    );
                        })
                );
    }

    private Mono<LocationResponse> fetchFromOpenMeteo(String city) {

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoGeocodingResponse.class).transformDeferred(
                RetryOperator.of(locationRetry)).transformDeferred(
                        CircuitBreakerOperator.of(locationCircuitBreaker))

                .flatMap(response -> {

                    if (response.results() == null
                            || response.results().isEmpty()) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "City not found: " + city
                                )
                        );
                    }

                    var result =
                            response.results().getFirst();

                    return Mono.just(
                            new LocationResponse(
                                    result.name(),
                                    result.country(),
                                    result.country_code(),
                                    result.latitude(),
                                    result.longitude(),
                                    result.timezone()));
                });
    }
}