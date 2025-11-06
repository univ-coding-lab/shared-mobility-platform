package com.next.iotsimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Simulated vehicle with sensor data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedVehicle {

    private String vehicleId;
    private double latitude;
    private double longitude;
    private int batteryLevel; // 0-100%
    private double speed; // km/h
    private double heading; // degrees from north (0-360)
    private boolean isMoving;
    private String rentalId;
    private LocalDateTime lastUpdate;

    private static final Random random = new Random();

    // Seoul area boundaries
    private static final double SEOUL_LAT_MIN = 37.45;
    private static final double SEOUL_LAT_MAX = 37.65;
    private static final double SEOUL_LON_MIN = 126.85;
    private static final double SEOUL_LON_MAX = 127.05;

    // Movement parameters
    private static final double MAX_SPEED_KMH = 30.0; // Max 30 km/h (electric scooter speed)
    private static final double MOVEMENT_STEP = 0.001; // ~111 meters per step
    private static final double BATTERY_DRAIN_PER_KM = 2; // 2% per km

    /**
     * Create a new simulated vehicle at random Seoul location
     */
    public static SimulatedVehicle createRandom(String vehicleId) {
        return SimulatedVehicle.builder()
                .vehicleId(vehicleId)
                .latitude(randomLatitude())
                .longitude(randomLongitude())
                .batteryLevel(80 + random.nextInt(21)) // 80-100%
                .speed(0.0)
                .heading(random.nextDouble() * 360)
                .isMoving(false)
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    /**
     * Simulate vehicle movement
     */
    public void move() {
        if (!isMoving || batteryLevel < 10) {
            // Stop if battery is too low
            speed = 0.0;
            isMoving = false;
            return;
        }

        // Update heading (slight random variation)
        heading += (random.nextDouble() - 0.5) * 30; // +/- 15 degrees
        if (heading < 0) heading += 360;
        if (heading >= 360) heading -= 360;

        // Update speed (with variation)
        speed = 15.0 + random.nextDouble() * 15.0; // 15-30 km/h

        // Calculate movement (convert speed to lat/lon delta)
        double radians = Math.toRadians(heading);
        double latDelta = Math.cos(radians) * MOVEMENT_STEP;
        double lonDelta = Math.sin(radians) * MOVEMENT_STEP;

        // Update position
        double newLat = latitude + latDelta;
        double newLon = longitude + lonDelta;

        // Keep within Seoul boundaries
        if (newLat >= SEOUL_LAT_MIN && newLat <= SEOUL_LAT_MAX) {
            latitude = newLat;
        } else {
            // Bounce back
            heading = (heading + 180) % 360;
        }

        if (newLon >= SEOUL_LON_MIN && newLon <= SEOUL_LON_MAX) {
            longitude = newLon;
        } else {
            // Bounce back
            heading = (heading + 180) % 360;
        }

        // Drain battery based on distance traveled
        double distanceKm = MOVEMENT_STEP * 111; // Approximate km
        int batteryDrain = (int) Math.ceil(distanceKm * BATTERY_DRAIN_PER_KM);
        batteryLevel = Math.max(0, batteryLevel - batteryDrain);

        lastUpdate = LocalDateTime.now();
    }

    /**
     * Start vehicle movement (rental started)
     */
    public void startRental(String rentalId) {
        this.rentalId = rentalId;
        this.isMoving = true;
        this.speed = 0.0;
    }

    /**
     * Stop vehicle movement (rental ended)
     */
    public void endRental() {
        this.rentalId = null;
        this.isMoving = false;
        this.speed = 0.0;
    }

    /**
     * Charge battery (simulate charging station)
     */
    public void charge(int amount) {
        batteryLevel = Math.min(100, batteryLevel + amount);
    }

    /**
     * Random latitude in Seoul
     */
    private static double randomLatitude() {
        return SEOUL_LAT_MIN + random.nextDouble() * (SEOUL_LAT_MAX - SEOUL_LAT_MIN);
    }

    /**
     * Random longitude in Seoul
     */
    private static double randomLongitude() {
        return SEOUL_LON_MIN + random.nextDouble() * (SEOUL_LON_MAX - SEOUL_LON_MIN);
    }

    /**
     * Check if vehicle needs charging
     */
    public boolean needsCharging() {
        return batteryLevel < 20;
    }

    /**
     * Check if vehicle is critically low
     */
    public boolean isCriticallyLow() {
        return batteryLevel < 10;
    }
}
