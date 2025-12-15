package com.next.iotsimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedVehicle {

    private String vehicleId;
    private double latitude;
    private double longitude;
    private int batteryLevel;
    private double speed;
    private double heading;
    private boolean isMoving;
    private String rentalId;
    private LocalDateTime lastUpdate;

    private static final Random random = new Random();

    private static final double SEOUL_LAT_MIN = 37.45;
    private static final double SEOUL_LAT_MAX = 37.65;
    private static final double SEOUL_LON_MIN = 126.85;
    private static final double SEOUL_LON_MAX = 127.05;

    private static final double MAX_SPEED_KMH = 30.0;
    private static final double MOVEMENT_STEP = 0.001;
    private static final double BATTERY_DRAIN_PER_KM = 2;

    public static SimulatedVehicle createRandom(String vehicleId) {
        return SimulatedVehicle.builder()
                .vehicleId(vehicleId)
                .latitude(randomLatitude())
                .longitude(randomLongitude())
                .batteryLevel(80 + random.nextInt(21))
                .speed(0.0)
                .heading(random.nextDouble() * 360)
                .isMoving(false)
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    public void move() {
        if (!isMoving || batteryLevel < 10) {

            speed = 0.0;
            isMoving = false;
            return;
        }

        heading += (random.nextDouble() - 0.5) * 30;
        if (heading < 0) heading += 360;
        if (heading >= 360) heading -= 360;

        speed = 15.0 + random.nextDouble() * 15.0;

        double radians = Math.toRadians(heading);
        double latDelta = Math.cos(radians) * MOVEMENT_STEP;
        double lonDelta = Math.sin(radians) * MOVEMENT_STEP;

        double newLat = latitude + latDelta;
        double newLon = longitude + lonDelta;

        if (newLat >= SEOUL_LAT_MIN && newLat <= SEOUL_LAT_MAX) {
            latitude = newLat;
        } else {

            heading = (heading + 180) % 360;
        }

        if (newLon >= SEOUL_LON_MIN && newLon <= SEOUL_LON_MAX) {
            longitude = newLon;
        } else {

            heading = (heading + 180) % 360;
        }

        double distanceKm = MOVEMENT_STEP * 111;
        int batteryDrain = (int) Math.ceil(distanceKm * BATTERY_DRAIN_PER_KM);
        batteryLevel = Math.max(0, batteryLevel - batteryDrain);

        lastUpdate = LocalDateTime.now();
    }

    public void startRental(String rentalId) {
        this.rentalId = rentalId;
        this.isMoving = true;
        this.speed = 0.0;
    }

    public void endRental() {
        this.rentalId = null;
        this.isMoving = false;
        this.speed = 0.0;
    }

    public void charge(int amount) {
        batteryLevel = Math.min(100, batteryLevel + amount);
    }

    private static double randomLatitude() {
        return SEOUL_LAT_MIN + random.nextDouble() * (SEOUL_LAT_MAX - SEOUL_LAT_MIN);
    }

    private static double randomLongitude() {
        return SEOUL_LON_MIN + random.nextDouble() * (SEOUL_LON_MAX - SEOUL_LON_MIN);
    }

    public boolean needsCharging() {
        return batteryLevel < 20;
    }

    public boolean isCriticallyLow() {
        return batteryLevel < 10;
    }
}
