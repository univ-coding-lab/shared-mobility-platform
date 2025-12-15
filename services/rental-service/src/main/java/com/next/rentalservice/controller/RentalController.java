package com.next.rentalservice.controller;

import com.next.common.domain.dto.ApiResponse;
import com.next.common.domain.model.Rental;
import com.next.rentalservice.dto.RentalEndRequest;
import com.next.rentalservice.dto.RentalResponse;
import com.next.rentalservice.dto.RentalStartRequest;
import com.next.rentalservice.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RentalResponse>> startRental(@Valid @RequestBody RentalStartRequest request) {
        Rental rental = rentalService.startRental(
                request.getUserId(),
                request.getVehicleId(),
                request.getLatitude(),
                request.getLongitude(),
                request.getBatteryLevel()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rental started", RentalResponse.from(rental)));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ApiResponse<RentalResponse>> endRental(
            @PathVariable String id,
            @Valid @RequestBody RentalEndRequest request) {
        Rental rental = rentalService.endRental(
                id,
                request.getLatitude(),
                request.getLongitude(),
                request.getBatteryLevel()
        );
        return ResponseEntity.ok(ApiResponse.success("Rental ended", RentalResponse.from(rental)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> getRental(@PathVariable String id) {
        Rental rental = rentalService.getRental(id);
        return ResponseEntity.ok(ApiResponse.success(RentalResponse.from(rental)));
    }
}
