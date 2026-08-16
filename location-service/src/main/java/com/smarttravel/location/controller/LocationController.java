package com.smarttravel.location.controller;

import com.smarttravel.location.dto.LocationResponse;
import com.smarttravel.location.service.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public Mono<LocationResponse> search(@RequestParam String city) {
        return locationService.findByCity(city);
    }
}
