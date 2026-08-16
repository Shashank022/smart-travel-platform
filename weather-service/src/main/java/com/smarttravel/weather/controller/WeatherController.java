package com.smarttravel.weather.controller;

import com.smarttravel.weather.dto.WeatherResponse;
import com.smarttravel.weather.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public Mono<WeatherResponse> current(@RequestParam double latitude, @RequestParam double longitude) {
        return weatherService.current(latitude, longitude);
    }
}
