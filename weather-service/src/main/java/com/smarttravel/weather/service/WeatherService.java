package com.smarttravel.weather.service;

import com.smarttravel.weather.dto.OpenMeteoWeatherResponse;
import com.smarttravel.weather.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {
    private final WebClient webClient;

    public WeatherService(WebClient.Builder builder,
                          @Value("${clients.open-meteo-weather.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<WeatherResponse> current(double latitude, double longitude) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,apparent_temperature,weather_code,wind_speed_10m")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoWeatherResponse.class)
                .map(response -> {
                    var current = response.current();
                    return new WeatherResponse(
                            current.temperature_2m(),
                            current.apparent_temperature(),
                            current.wind_speed_10m(),
                            current.weather_code(),
                            condition(current.weather_code()));
                });
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
