package com.next.rentalservice.repository;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, String> {
    List<Rental> findByUserId(String userId);
    List<Rental> findByVehicleIdAndStatus(String vehicleId, RentalStatus status);
    Optional<Rental> findTopByUserIdAndStatusOrderByStartTimeDesc(String userId, RentalStatus status);
}
