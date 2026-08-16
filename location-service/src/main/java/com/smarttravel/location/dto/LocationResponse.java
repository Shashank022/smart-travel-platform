package com.smarttravel.location.dto;

public record LocationResponse(
        String city,
        String country,
        String countryCode,
        double latitude,
        double longitude,
        String timezone
) {}
