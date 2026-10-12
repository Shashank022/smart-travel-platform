package com.smarttravel.tripplanner.service;

import com.smarttravel.tripplanner.dto.CreateTripRequest;
import com.smarttravel.tripplanner.dto.TravelSummaryResponse;
import com.smarttravel.tripplanner.entity.TripEntity;
import com.smarttravel.tripplanner.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class SavedTripService {

    private final TripPlannerService tripPlannerService;
    private final TripRepository tripRepository;

    public SavedTripService(
            TripPlannerService tripPlannerService,
            TripRepository tripRepository) {

        this.tripPlannerService = tripPlannerService;
        this.tripRepository = tripRepository;
    }

    public Mono<TripEntity> createTrip(CreateTripRequest request) {

        return tripPlannerService
                .summary(
                        request.city(),
                        request.fromCurrency(),
                        request.toCurrency(),
                        request.budget()
                )
                .map(this::toEntity)
                .flatMap(tripRepository::save);
    }

    public Flux<TripEntity> getAllTrips() {
        return tripRepository.findAll();
    }

    public Mono<TripEntity> getTrip(Long id) {

        return tripRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Trip not found: " + id
                                )
                        )
                );
    }

    public Mono<Void> deleteTrip(Long id) {

        return getTrip(id)
                .flatMap(tripRepository::delete);
    }

    private TripEntity toEntity(
            TravelSummaryResponse summary) {

        var destination = summary.destination();
        var weather = summary.weather();
        var currency = summary.currency();

        return new TripEntity(
                null,

                destination.city(),
                destination.country(),
                destination.countryCode(),

                destination.latitude(),
                destination.longitude(),
                destination.timezone(),

                currency.from(),
                currency.to(),

                currency.rate(),
                currency.amount(),
                currency.convertedAmount(),

                weather.temperatureC(),
                weather.apparentTemperatureC(),
                weather.windSpeedKmh(),
                weather.weatherCode(),
                weather.condition(),

                summary.generatedAt(),
                Instant.now()
        );
    }
}