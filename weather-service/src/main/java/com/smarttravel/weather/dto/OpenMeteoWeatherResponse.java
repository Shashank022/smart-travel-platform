package com.smarttravel.weather.dto;

public record OpenMeteoWeatherResponse(Current current) {
    public record Current(
            double temperature_2m,
            double apparent_temperature,
            double wind_speed_10m,
            int weather_code
    ) {}
}
