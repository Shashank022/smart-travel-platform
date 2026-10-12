package com.smarttravel.weather.controller;

import com.smarttravel.weather.dto.WeatherResponse;
import com.smarttravel.weather.service.WeatherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Local-only test hook for exercising the weather circuit breaker with a
 * repeatable failure burst. Disabled unless explicitly enabled by config.
 */
@RestController
@RequestMapping("/api/v1/test/resilience")
@ConditionalOnProperty(
        prefix = "smarttravel.resilience-test",
        name = "enabled",
        havingValue = "true"
)
public class ResilienceTestController {

    private final WeatherService weatherService;

    public ResilienceTestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping("/weather-failure")
    public Mono<WeatherResponse> simulateWeatherFailure() {
        return weatherService.simulateProviderFailure();
    }
}
