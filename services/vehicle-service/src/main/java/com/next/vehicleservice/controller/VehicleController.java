package com.next.vehicleservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Vehicle;
import com.next.vehicleservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getAvailableVehicles() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getAvailableVehicles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getVehicle(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.createVehicle(vehicle)));
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
}
