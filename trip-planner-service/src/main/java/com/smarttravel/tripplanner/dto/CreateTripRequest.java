package com.smarttravel.tripplanner.dto;

import java.math.BigDecimal;

public record CreateTripRequest(
        String city,
        String fromCurrency,
        String toCurrency,
        BigDecimal budget
) {
}