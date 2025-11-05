package com.next.vehicleservice.repository;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByStatus(VehicleStatus status);
    Optional<Vehicle> findBySerialNumber(String serialNumber);
    List<Vehicle> findByStatusAndBatteryLevelGreaterThan(VehicleStatus status, Integer batteryLevel);
}
