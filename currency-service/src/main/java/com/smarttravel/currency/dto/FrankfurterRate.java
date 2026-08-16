package com.smarttravel.currency.dto;

import java.math.BigDecimal;

public record FrankfurterRate(
        String date,
        String base,
        String quote,
        BigDecimal rate
) {}
