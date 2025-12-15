package com.next.vehicleservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.dto.VehicleRequest;
import com.next.vehicleservice.dto.VehicleResponse;
import com.next.vehicleservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAvailableVehicles() {
        List<VehicleResponse> responses = vehicleService.getAvailableVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable String id) {
        Vehicle vehicle = vehicleService.getVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(VehicleResponse.from(vehicle)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(@Valid @RequestBody VehicleRequest request) {
        Vehicle vehicle = vehicleService.createVehicle(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created", VehicleResponse.from(vehicle)));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockVehicle(@PathVariable String id) {
        vehicleService.lockVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle locked", null));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockVehicle(@PathVariable String id) {
        vehicleService.unlockVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle unlocked", null));
    }

    private Vehicle toEntity(VehicleRequest request) {
        return Vehicle.builder()
                .serialNumber(request.getSerialNumber())
                .type(request.getType())
                .model(request.getModel())
                .manufacturer(request.getManufacturer())
                .manufacturingYear(request.getManufacturingYear())
                .lastKnownLatitude(request.getLatitude())
                .lastKnownLongitude(request.getLongitude())
                .batteryLevel(request.getBatteryLevel())
                .iotDeviceId(request.getIotDeviceId())
                .iotProtocol(request.getIotProtocol())
                .notes(request.getNotes())
                .build();
    }
}
