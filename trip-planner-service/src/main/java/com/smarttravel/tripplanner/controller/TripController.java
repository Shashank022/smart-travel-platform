package com.smarttravel.tripplanner.controller;

import com.smarttravel.tripplanner.dto.CreateTripRequest;
import com.smarttravel.tripplanner.entity.TripEntity;
import com.smarttravel.tripplanner.service.SavedTripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final SavedTripService savedTripService;

    public TripController(
            SavedTripService savedTripService) {

        this.savedTripService = savedTripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TripEntity> createTrip(
            @RequestBody CreateTripRequest request) {

        return savedTripService.createTrip(request);
    }

    @GetMapping
    public Flux<TripEntity> getAllTrips() {

        return savedTripService.getAllTrips();
    }

    @GetMapping("/{id}")
    public Mono<TripEntity> getTrip(
            @PathVariable Long id) {

        return savedTripService.getTrip(id);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTrip(
            @PathVariable Long id) {

        return savedTripService
                .deleteTrip(id)
                .thenReturn(
                        ResponseEntity.noContent().build()
                );
    }
}