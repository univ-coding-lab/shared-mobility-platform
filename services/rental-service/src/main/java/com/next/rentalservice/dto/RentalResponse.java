package com.next.rentalservice.dto;

import com.next.common.domain.enums.RentalStatus;
import com.next.common.domain.model.Rental;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {

    private String id;
    private String userId;
    private String vehicleId;
    private RentalStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Integer startBatteryLevel;
    private Integer endBatteryLevel;
    private Double distanceKm;
    private Long durationMinutes;
    private BigDecimal basePrice;
    private BigDecimal distancePrice;
    private BigDecimal timePrice;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RentalResponse from(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUserId())
                .vehicleId(rental.getVehicleId())
                .status(rental.getStatus())
                .startTime(rental.getStartTime())
                .endTime(rental.getEndTime())
                .startLatitude(rental.getStartLatitude())
                .startLongitude(rental.getStartLongitude())
                .endLatitude(rental.getEndLatitude())
                .endLongitude(rental.getEndLongitude())
                .startBatteryLevel(rental.getStartBatteryLevel())
                .endBatteryLevel(rental.getEndBatteryLevel())
                .distanceKm(rental.getDistanceKm())
                .durationMinutes(rental.getDurationMinutes())
                .basePrice(rental.getBasePrice())
                .distancePrice(rental.getDistancePrice())
                .timePrice(rental.getTimePrice())
                .totalPrice(rental.getTotalPrice())
                .paymentStatus(rental.getPaymentStatus())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }
}
