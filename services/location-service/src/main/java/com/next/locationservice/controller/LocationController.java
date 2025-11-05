package com.next.locationservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Location;
import com.next.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Location>> saveLocation(@RequestBody Location location) {
        return ResponseEntity.ok(ApiResponse.success(locationService.saveLocation(location)));
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<ApiResponse<Location>> getLatestLocation(@PathVariable String vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getLatestLocation(vehicleId)));
    }
}
