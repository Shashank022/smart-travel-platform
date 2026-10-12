package com.smarttravel.tripplanner.repository;

import com.smarttravel.tripplanner.entity.TripEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TripRepository
        extends ReactiveCrudRepository<TripEntity, Long> {
}