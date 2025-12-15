package com.next.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service fallback triggered");
        return Mono.just(createFallbackResponse("User Service"));
    }

    @GetMapping("/vehicle-service")
    public Mono<ResponseEntity<Map<String, Object>>> vehicleServiceFallback() {
        log.warn("Vehicle service fallback triggered");
        return Mono.just(createFallbackResponse("Vehicle Service"));
    }

    @GetMapping("/rental-service")
    public Mono<ResponseEntity<Map<String, Object>>> rentalServiceFallback() {
        log.warn("Rental service fallback triggered");
        return Mono.just(createFallbackResponse("Rental Service"));
    }

    @GetMapping("/location-service")
    public Mono<ResponseEntity<Map<String, Object>>> locationServiceFallback() {
        log.warn("Location service fallback triggered");
        return Mono.just(createFallbackResponse("Location Service"));
    }

    @GetMapping("/battery-service")
    public Mono<ResponseEntity<Map<String, Object>>> batteryServiceFallback() {
        log.warn("Battery service fallback triggered");
        return Mono.just(createFallbackResponse("Battery Service"));
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("service", serviceName);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
