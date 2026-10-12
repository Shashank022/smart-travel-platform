package com.smarttravel.weather.service;

import com.smarttravel.weather.dto.OpenMeteoWeatherResponse;
import com.smarttravel.weather.dto.WeatherResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;

@Service
public class WeatherService {

    private static final Logger log =
            LoggerFactory.getLogger(WeatherService.class);

    private static final Duration WEATHER_CACHE_TTL =
            Duration.ofMinutes(15);

    private final WebClient webClient;

    private final ReactiveRedisTemplate<String, WeatherResponse>
            redisTemplate;

    private final CircuitBreaker weatherCircuitBreaker;

    private final Retry weatherRetry;


    public WeatherService(
            WebClient.Builder builder,
            ReactiveRedisTemplate<String, WeatherResponse> redisTemplate,
            @Value("${clients.open-meteo-weather.base-url}")
                    String baseUrl, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.webClient =
                builder.baseUrl(baseUrl).build();

        this.redisTemplate = redisTemplate;

        this.weatherCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("weatherApi");

        this.weatherRetry =
                retryRegistry.retry("weatherApi");
    }

    public Mono<WeatherResponse> current(
            double latitude,
            double longitude) {

        String cacheKey = buildCacheKey(
                latitude,
                longitude
        );

        return redisTemplate
                .opsForValue()
                .get(cacheKey)

                .doOnNext(weather ->
                        log.info(
                                "Redis HIT: {}",
                                cacheKey
                        )
                )

                .onErrorResume(exception -> {
                    log.warn(
                            "Redis read failed for {}",
                            cacheKey,
                            exception
                    );

                    return Mono.empty();
                })

                .switchIfEmpty(
                        Mono.defer(() -> {

                            log.info(
                                    "Redis MISS: {}",
                                    cacheKey
                            );

                            return fetchFromOpenMeteo(
                                    latitude,
                                    longitude
                            ).flatMap(weather -> redisTemplate
                                            .opsForValue()
                                            .set(
                                                    cacheKey,
                                                    weather,
                                                    WEATHER_CACHE_TTL
                                            )

                                            .doOnNext(saved ->
                                                    log.info(
                                                            "Stored {} in Redis with TTL {}",
                                                            cacheKey,
                                                            WEATHER_CACHE_TTL
                                                    )
                                            ).thenReturn(weather)

                                            .onErrorResume(exception -> {

                                                log.warn(
                                                        "Unable to cache {}",
                                                        cacheKey,
                                                        exception
                                                );

                                                return Mono.just(weather);
                                            })
                            );
                        })
                );
    }

    private Mono<WeatherResponse> fetchFromOpenMeteo(
            double latitude,
            double longitude) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam(
                                "current",
                                "temperature_2m,apparent_temperature,weather_code,wind_speed_10m"
                        )
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoWeatherResponse.class).transformDeferred(
                RetryOperator.of(weatherRetry)
        )
                .transformDeferred(
                        CircuitBreakerOperator.of(weatherCircuitBreaker)
                )
                .map(response -> {
                    var current = response.current();
                    return new WeatherResponse(
                            current.temperature_2m(),
                            current.apparent_temperature(),
                            current.wind_speed_10m(),
                            current.weather_code(),
                            condition(current.weather_code())
                    );
                });
    }

    private String buildCacheKey(
            double latitude,
            double longitude) {

        return String.format(
                Locale.ROOT,
                "weather:%.4f:%.4f",
                latitude,
                longitude
        );
    }

    private String condition(int code) {

        return switch (code) {
            case 0 -> "CLEAR";
            case 1, 2 -> "PARTLY_CLOUDY";
            case 3 -> "OVERCAST";
            case 45, 48 -> "FOG";
            case 51, 53, 55, 56, 57 -> "DRIZZLE";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "RAIN";
            case 71, 73, 75, 77, 85, 86 -> "SNOW";
            case 95, 96, 99 -> "THUNDERSTORM";
            default -> "UNKNOWN";
        };
    }
}