package com.next.rentalservice.saga;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalSagaRepository extends CrudRepository<RentalSaga, String> {

    Optional<RentalSaga> findByRentalId(String rentalId);

    Optional<RentalSaga> findByVehicleId(String vehicleId);
}
