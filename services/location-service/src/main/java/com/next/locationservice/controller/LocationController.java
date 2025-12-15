package com.next.locationservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Location;
import com.next.locationservice.dto.LocationRequest;
import com.next.locationservice.dto.LocationResponse;
import com.next.locationservice.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponse>> saveLocation(@Valid @RequestBody LocationRequest request) {
        Location location = locationService.saveLocation(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location saved", LocationResponse.from(location)));
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<ApiResponse<LocationResponse>> getLatestLocation(@PathVariable String vehicleId) {
        Location location = locationService.getLatestLocation(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(LocationResponse.from(location)));
    }

    private Location toEntity(LocationRequest request) {
        return Location.builder()
                .vehicleId(request.getVehicleId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .altitude(request.getAltitude())
                .speed(request.getSpeed())
                .heading(request.getHeading())
                .accuracy(request.getAccuracy())
                .source(request.getSource())
                .isMoving(request.getIsMoving())
                .rentalId(request.getRentalId())
                .build();
    }
}
