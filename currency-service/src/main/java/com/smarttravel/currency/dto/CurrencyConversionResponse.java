package com.smarttravel.currency.dto;

import java.math.BigDecimal;

public record CurrencyConversionResponse(
        String from,
        String to,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {}
