package com.next.common.domain.model;

import com.next.common.domain.enums.RentalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Rental domain model representing a vehicle rental transaction
 */
@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental extends BaseEntity {

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotBlank(message = "Vehicle ID is required")
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @NotNull(message = "Rental status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RentalStatus status;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "distance_price", precision = 10, scale = 2)
    private BigDecimal distancePrice;

    @Column(name = "time_price", precision = 10, scale = 2)
    private BigDecimal timePrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "start_battery_level")
    private Integer startBatteryLevel;

    @Column(name = "end_battery_level")
    private Integer endBatteryLevel;

    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Calculate rental duration in minutes
     */
    public Long calculateDuration() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        }
        return null;
    }

    /**
     * Check if rental is active
     */
    public boolean isActive() {
        return status == RentalStatus.ACTIVE || status == RentalStatus.PAUSED;
    }

    /**
     * Complete the rental
     */
    public void complete(Double endLat, Double endLon, Integer endBattery) {
        this.endTime = LocalDateTime.now();
        this.endLatitude = endLat;
        this.endLongitude = endLon;
        this.endBatteryLevel = endBattery;
        this.durationMinutes = calculateDuration();
        this.status = RentalStatus.COMPLETED;
    }
}
