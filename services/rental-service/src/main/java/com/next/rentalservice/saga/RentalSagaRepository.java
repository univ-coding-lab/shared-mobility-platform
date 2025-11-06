package com.next.rentalservice.saga;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Rental Saga state management
 * Uses Redis for fast access and automatic TTL
 */
@Repository
public interface RentalSagaRepository extends CrudRepository<RentalSaga, String> {

    /**
     * Find saga by rental ID
     */
    Optional<RentalSaga> findByRentalId(String rentalId);

    /**
     * Find saga by vehicle ID
     */
    Optional<RentalSaga> findByVehicleId(String vehicleId);
}
