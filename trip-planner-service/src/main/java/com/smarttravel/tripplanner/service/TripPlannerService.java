package com.smarttravel.tripplanner.service;

import com.smarttravel.tripplanner.dto.CurrencyConversionResponse;
import com.smarttravel.tripplanner.dto.LocationResponse;
import com.smarttravel.tripplanner.dto.TravelSummaryResponse;
import com.smarttravel.tripplanner.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TripPlannerService {
    private final WebClient locationClient;
    private final WebClient weatherClient;
    private final WebClient currencyClient;

    public TripPlannerService(
            WebClient.Builder builder,
            @Value("${services.location.base-url}") String locationBaseUrl,
            @Value("${services.weather.base-url}") String weatherBaseUrl,
            @Value("${services.currency.base-url}") String currencyBaseUrl) {
        this.locationClient = builder.clone().baseUrl(locationBaseUrl).build();
        this.weatherClient = builder.clone().baseUrl(weatherBaseUrl).build();
        this.currencyClient = builder.clone().baseUrl(currencyBaseUrl).build();
    }

    public Mono<TravelSummaryResponse> summary(
            String city, String fromCurrency, String toCurrency, BigDecimal budget) {

        Mono<LocationResponse> locationMono = locationClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/locations/search")
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToMono(LocationResponse.class)
                .cache();

        Mono<WeatherResponse> weatherMono = locationMono.flatMap(location -> weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/weather/current")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class));

        Mono<CurrencyConversionResponse> currencyMono = currencyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/currency/convert")
                        .queryParam("from", fromCurrency)
                        .queryParam("to", toCurrency)
                        .queryParam("amount", budget)
                        .build())
                .retrieve()
                .bodyToMono(CurrencyConversionResponse.class);

        return Mono.zip(locationMono, weatherMono, currencyMono)
                .map(tuple -> new TravelSummaryResponse(
                        tuple.getT1(), tuple.getT2(), tuple.getT3(), Instant.now()));
    }
}
