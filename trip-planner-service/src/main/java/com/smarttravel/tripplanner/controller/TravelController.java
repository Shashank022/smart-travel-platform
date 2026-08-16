package com.smarttravel.tripplanner.controller;

import com.smarttravel.tripplanner.dto.TravelSummaryResponse;
import com.smarttravel.tripplanner.service.TripPlannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/travel")
public class TravelController {
    private final TripPlannerService tripPlannerService;

    public TravelController(TripPlannerService tripPlannerService) {
        this.tripPlannerService = tripPlannerService;
    }

    @GetMapping("/summary")
    public Mono<TravelSummaryResponse> summary(
            @RequestParam String city,
            @RequestParam(defaultValue = "USD") String fromCurrency,
            @RequestParam(defaultValue = "EUR") String toCurrency,
            @RequestParam BigDecimal budget) {
        return tripPlannerService.summary(city, fromCurrency, toCurrency, budget);
    }
}
