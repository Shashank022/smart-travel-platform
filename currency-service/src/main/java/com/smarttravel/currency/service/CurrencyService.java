package com.smarttravel.currency.service;

import com.smarttravel.currency.dto.CurrencyConversionResponse;
import com.smarttravel.currency.dto.FrankfurterRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CurrencyService {
    private final WebClient webClient;

    public CurrencyService(WebClient.Builder builder,
                           @Value("${clients.frankfurter.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<CurrencyConversionResponse> convert(String from, String to, BigDecimal amount) {
        String normalizedFrom = from.toUpperCase();
        String normalizedTo = to.toUpperCase();

        if (normalizedFrom.equals(normalizedTo)) {
            return Mono.just(new CurrencyConversionResponse(
                    normalizedFrom, normalizedTo, BigDecimal.ONE, amount, amount));
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/rates")
                        .queryParam("base", normalizedFrom)
                        .queryParam("quotes", normalizedTo)
                        .build())
                .retrieve()
                .bodyToFlux(FrankfurterRate.class)
                .collectList()
                .flatMap(rates -> buildResponse(rates, normalizedFrom, normalizedTo, amount));
    }

    private Mono<CurrencyConversionResponse> buildResponse(
            List<FrankfurterRate> rates, String from, String to, BigDecimal amount) {
        if (rates.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Exchange rate unavailable"));
        }
        BigDecimal rate = rates.getFirst().rate();
        BigDecimal converted = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return Mono.just(new CurrencyConversionResponse(from, to, rate, amount, converted));
    }
}
