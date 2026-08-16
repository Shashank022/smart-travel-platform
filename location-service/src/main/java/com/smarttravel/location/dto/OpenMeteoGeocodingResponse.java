package com.smarttravel.location.dto;

import java.util.List;

public record OpenMeteoGeocodingResponse(List<Result> results) {
    public record Result(
            String name,
            double latitude,
            double longitude,
            String country,
            String country_code,
            String timezone
    ) {}
}
