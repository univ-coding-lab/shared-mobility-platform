package com.next.vehicleservice.service;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.domain.enums.VehicleType;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.port.IoTDevicePort;
import com.next.vehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private IoTDevicePort iotDevicePort;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .serialNumber("SN-001")
                .type(VehicleType.E_SCOOTER)
                .model("Model X")
                .manufacturer("TechCo")
                .status(VehicleStatus.AVAILABLE)
                .batteryLevel(85)
                .iotDeviceId("device-123")
                .build();
        vehicle.setId("vehicle-1");
    }

    @Test
    void getAvailableVehicles_ShouldReturnAvailableVehiclesWithSufficientBattery() {

        Vehicle vehicle2 = Vehicle.builder()
                .serialNumber("SN-002")
                .type(VehicleType.BICYCLE)
                .model("Model Y")
                .manufacturer("BikeInc")
                .status(VehicleStatus.AVAILABLE)
                .batteryLevel(90)
                .build();
        vehicle2.setId("vehicle-2");

        when(vehicleRepository.findByStatusAndBatteryLevelGreaterThan(VehicleStatus.AVAILABLE, 20))
                .thenReturn(Arrays.asList(vehicle, vehicle2));

        List<Vehicle> result = vehicleService.getAvailableVehicles();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(vehicleRepository).findByStatusAndBatteryLevelGreaterThan(VehicleStatus.AVAILABLE, 20);
    }

    @Test
    void getVehicle_WithValidId_ShouldReturnVehicle() {

        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getVehicle("vehicle-1");

        assertNotNull(result);
        assertEquals("vehicle-1", result.getId());
        assertEquals(VehicleType.E_SCOOTER, result.getType());
        verify(vehicleRepository).findById("vehicle-1");
    }

    @Test
    void getVehicle_WithInvalidId_ShouldThrowResourceNotFoundException() {

        when(vehicleRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicle("invalid-id"));
        verify(vehicleRepository).findById("invalid-id");
    }

    @Test
    void createVehicle_ShouldSetStatusToAvailableAndSave() {

        Vehicle newVehicle = Vehicle.builder()
                .serialNumber("SN-NEW")
                .type(VehicleType.E_SCOOTER)
                .model("New Model")
                .manufacturer("NewCo")
                .batteryLevel(100)
                .build();

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(newVehicle);

        Vehicle result = vehicleService.createVehicle(newVehicle);

        assertNotNull(result);
        assertEquals(VehicleStatus.AVAILABLE, result.getStatus());
        verify(vehicleRepository).save(newVehicle);
    }

    @Test
    void updateVehicleStatus_WithValidId_ShouldUpdateStatus() {

        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        vehicleService.updateVehicleStatus("vehicle-1", VehicleStatus.MAINTENANCE);

        assertEquals(VehicleStatus.MAINTENANCE, vehicle.getStatus());
        verify(vehicleRepository).findById("vehicle-1");
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void lockVehicle_WithIoTDevice_ShouldCallIoTPort() {

        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));

        vehicleService.lockVehicle("vehicle-1");

        verify(vehicleRepository).findById("vehicle-1");
        verify(iotDevicePort).lockVehicle("device-123");
    }

    @Test
    void lockVehicle_WithoutIoTDevice_ShouldNotCallIoTPort() {

        vehicle.setIotDeviceId(null);
        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));

        vehicleService.lockVehicle("vehicle-1");

        verify(vehicleRepository).findById("vehicle-1");
        verify(iotDevicePort, never()).lockVehicle(anyString());
    }

    @Test
    void unlockVehicle_WithIoTDevice_ShouldCallIoTPort() {

        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));

        vehicleService.unlockVehicle("vehicle-1");

        verify(vehicleRepository).findById("vehicle-1");
        verify(iotDevicePort).unlockVehicle("device-123");
    }

    @Test
    void unlockVehicle_WithoutIoTDevice_ShouldNotCallIoTPort() {

        vehicle.setIotDeviceId(null);
        when(vehicleRepository.findById("vehicle-1")).thenReturn(Optional.of(vehicle));

        vehicleService.unlockVehicle("vehicle-1");

        verify(vehicleRepository).findById("vehicle-1");
        verify(iotDevicePort, never()).unlockVehicle(anyString());
    }
}
