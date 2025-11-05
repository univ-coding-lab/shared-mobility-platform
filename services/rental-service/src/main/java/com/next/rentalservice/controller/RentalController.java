package com.next.rentalservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Rental;
import com.next.rentalservice.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Rental>> startRental(
            @RequestParam String userId,
            @RequestParam String vehicleId,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Integer batteryLevel) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.startRental(userId, vehicleId, lat, lon, batteryLevel)));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ApiResponse<Rental>> endRental(
            @PathVariable String id,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Integer batteryLevel) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.endRental(id, lat, lon, batteryLevel)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rental>> getRental(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.getRental(id)));
    }
}
