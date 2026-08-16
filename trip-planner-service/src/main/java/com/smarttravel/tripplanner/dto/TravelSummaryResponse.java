package com.smarttravel.tripplanner.dto;

import java.time.Instant;

public record TravelSummaryResponse(
        LocationResponse destination,
        WeatherResponse weather,
        CurrencyConversionResponse currency,
        Instant generatedAt
) {}
