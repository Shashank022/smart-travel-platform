package com.smarttravel.tripplanner.dto;

public record WeatherResponse(double temperatureC, double apparentTemperatureC, double windSpeedKmh, int weatherCode, String condition) {}
