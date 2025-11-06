package com.next.iotsimulator.publisher;

import com.next.common.event.config.KafkaTopics;
import com.next.common.event.model.VehicleMovedEvent;
import com.next.common.event.publisher.EventPublisher;
import com.next.iotsimulator.model.SimulatedVehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Telemetry publisher
 * Publishes vehicle sensor data to Kafka and HTTP services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryPublisher {

    private final EventPublisher eventPublisher;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.location.url:http://localhost:8084}")
    private String locationServiceUrl;

    @Value("${services.battery.url:http://localhost:8085}")
    private String batteryServiceUrl;

    @Value("${simulator.http.enabled:true}")
    private boolean httpEnabled;

    /**
     * Publish vehicle telemetry to all channels
     */
    public void publishTelemetry(SimulatedVehicle vehicle) {
        // 1. Publish to Kafka (VehicleMovedEvent)
        publishToKafka(vehicle);

        // 2. Send to Location Service via HTTP (optional)
        if (httpEnabled && vehicle.isMoving()) {
            sendLocationData(vehicle);
        }

        // 3. Send to Battery Service via HTTP (optional, less frequently)
        if (httpEnabled && vehicle.needsCharging()) {
            sendBatteryData(vehicle);
        }
    }

    /**
     * Publish VehicleMovedEvent to Kafka
     */
    private void publishToKafka(SimulatedVehicle vehicle) {
        try {
            VehicleMovedEvent event = VehicleMovedEvent.builder()
                    .vehicleId(vehicle.getVehicleId())
                    .latitude(vehicle.getLatitude())
                    .longitude(vehicle.getLongitude())
                    .speed(vehicle.getSpeed())
                    .heading(vehicle.getHeading())
                    .isMoving(vehicle.isMoving())
                    .rentalId(vehicle.getRentalId())
                    .build();

            eventPublisher.publish(KafkaTopics.VEHICLE_MOVED, vehicle.getVehicleId(), event);
            log.debug("Published VehicleMovedEvent: vehicleId={}, lat={}, lon={}, speed={}km/h",
                    vehicle.getVehicleId(), vehicle.getLatitude(), vehicle.getLongitude(), vehicle.getSpeed());

        } catch (Exception e) {
            log.error("Failed to publish Kafka event for vehicle {}: {}",
                    vehicle.getVehicleId(), e.getMessage());
        }
    }

    /**
     * Send location data to Location Service
     */
    private void sendLocationData(SimulatedVehicle vehicle) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("vehicleId", vehicle.getVehicleId());
        locationData.put("latitude", vehicle.getLatitude());
        locationData.put("longitude", vehicle.getLongitude());
        locationData.put("speed", vehicle.getSpeed());
        locationData.put("heading", vehicle.getHeading());
        locationData.put("isMoving", vehicle.isMoving());
        locationData.put("rentalId", vehicle.getRentalId());
        locationData.put("source", "IOT_DEVICE");

        webClientBuilder.build()
                .post()
                .uri(locationServiceUrl + "/api/v1/locations")
                .bodyValue(locationData)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    log.debug("Location Service unavailable (normal if not running): {}",
                            error.getMessage());
                    return Mono.empty();
                })
                .subscribe(response ->
                        log.debug("Sent location data to Location Service: vehicleId={}",
                                vehicle.getVehicleId())
                );

    }

    /**
     * Send battery data to Battery Service
     */
    private void sendBatteryData(SimulatedVehicle vehicle) {
        Map<String, Object> batteryData = new HashMap<>();
        batteryData.put("vehicleId", vehicle.getVehicleId());
        batteryData.put("batteryLevel", vehicle.getBatteryLevel());
        batteryData.put("source", "IOT_DEVICE");
        batteryData.put("healthStatus", determineHealthStatus(vehicle.getBatteryLevel()));
        batteryData.put("estimatedRangeKm", vehicle.getBatteryLevel() * 2); // Simple formula

        webClientBuilder.build()
                .post()
                .uri(batteryServiceUrl + "/api/v1/batteries")
                .bodyValue(batteryData)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    log.debug("Battery Service unavailable (normal if not running): {}",
                            error.getMessage());
                    return Mono.empty();
                })
                .subscribe(response ->
                        log.debug("Sent battery data to Battery Service: vehicleId={}, level={}%",
                                vehicle.getVehicleId(), vehicle.getBatteryLevel())
                );
    }

    /**
     * Determine battery health status
     */
    private String determineHealthStatus(int batteryLevel) {
        if (batteryLevel >= 80) return "GOOD";
        if (batteryLevel >= 50) return "FAIR";
        if (batteryLevel >= 20) return "POOR";
        return "CRITICAL";
    }
}
