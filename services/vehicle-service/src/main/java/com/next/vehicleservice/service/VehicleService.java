package com.next.vehicleservice.service;

import com.next.common.domain.constants.BatteryConstants;
import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.port.IoTDevicePort;
import com.next.vehicleservice.repository.VehicleRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final IoTDevicePort iotDevicePort;

    @Transactional(readOnly = true)
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatusAndBatteryLevelGreaterThan(VehicleStatus.AVAILABLE, BatteryConstants.MINIMUM_FOR_RENTAL);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void updateVehicleStatus(String id, VehicleStatus status) {
        Vehicle vehicle = getVehicle(id);
        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} status updated to {}", id, status);
    }

    @Transactional
    @CircuitBreaker(name = "iotService", fallbackMethod = "lockVehicleFallback")
    @Retry(name = "iotService")
    public void lockVehicle(String id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getIotDeviceId() != null) {
            iotDevicePort.lockVehicle(vehicle.getIotDeviceId());
        }
        log.info("Vehicle {} locked", id);
    }

    public void lockVehicleFallback(String id, Exception e) {
        log.warn("IoT service unavailable for lock, marking vehicle for manual lock: vehicleId={}, error={}",
                id, e.getMessage());
        Vehicle vehicle = getVehicle(id);
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicle.setNotes("IoT lock failed - manual lock required: " + e.getMessage());
        vehicleRepository.save(vehicle);
    }

    @Transactional
    @CircuitBreaker(name = "iotService", fallbackMethod = "unlockVehicleFallback")
    @Retry(name = "iotService")
    public void unlockVehicle(String id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getIotDeviceId() != null) {
            iotDevicePort.unlockVehicle(vehicle.getIotDeviceId());
        }
        log.info("Vehicle {} unlocked", id);
    }

    public void unlockVehicleFallback(String id, Exception e) {
        log.warn("IoT service unavailable for unlock, marking vehicle for manual unlock: vehicleId={}, error={}",
                id, e.getMessage());
        Vehicle vehicle = getVehicle(id);
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicle.setNotes("IoT unlock failed - manual unlock required: " + e.getMessage());
        vehicleRepository.save(vehicle);
    }
}
