package com.smarttravel.weather.dto;

public record WeatherResponse(
        double temperatureC,
        double apparentTemperatureC,
        double windSpeedKmh,
        int weatherCode,
        String condition
) {}
