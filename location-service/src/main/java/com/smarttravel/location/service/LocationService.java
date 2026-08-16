package com.smarttravel.location.service;

import com.smarttravel.location.dto.LocationResponse;
import com.smarttravel.location.dto.OpenMeteoGeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class LocationService {
    private final WebClient webClient;

    public LocationService(WebClient.Builder builder,
                           @Value("${clients.open-meteo-geocoding.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<LocationResponse> findByCity(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoGeocodingResponse.class)
                .flatMap(response -> {
                    if (response.results() == null || response.results().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found: " + city));
                    }
                    var r = response.results().getFirst();
                    return Mono.just(new LocationResponse(
                            r.name(), r.country(), r.country_code(),
                            r.latitude(), r.longitude(), r.timezone()));
                });
    }
}
