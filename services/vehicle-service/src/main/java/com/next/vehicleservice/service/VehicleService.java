package com.next.vehicleservice.service;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.port.IoTDevicePort;
import com.next.vehicleservice.repository.VehicleRepository;
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
        return vehicleRepository.findByStatusAndBatteryLevelGreaterThan(VehicleStatus.AVAILABLE, 20);
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
    public void lockVehicle(String id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getIotDeviceId() != null) {
            iotDevicePort.lockVehicle(vehicle.getIotDeviceId());
        }
        log.info("Vehicle {} locked", id);
    }

    @Transactional
    public void unlockVehicle(String id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getIotDeviceId() != null) {
            iotDevicePort.unlockVehicle(vehicle.getIotDeviceId());
        }
        log.info("Vehicle {} unlocked", id);
    }
}
