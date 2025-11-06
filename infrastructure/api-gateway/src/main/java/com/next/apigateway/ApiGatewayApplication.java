package com.next.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application
 * Central entry point for all microservices
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure routes for all microservices
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service - Authentication (no JWT required)
                .route("user_service_auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("http://user-service:8081"))

                // User Service - Protected endpoints
                .route("user_service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("http://user-service:8081"))

                // Vehicle Service
                .route("vehicle_service", r -> r
                        .path("/api/v1/vehicles/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("vehicleServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/vehicle-service")))
                        .uri("http://vehicle-service:8082"))

                // Rental Service
                .route("rental_service", r -> r
                        .path("/api/v1/rentals/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("rentalServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/rental-service")))
                        .uri("http://rental-service:8083"))

                // Location Service
                .route("location_service", r -> r
                        .path("/api/v1/locations/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("locationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/location-service")))
                        .uri("http://location-service:8084"))

                // Battery Service
                .route("battery_service", r -> r
                        .path("/api/v1/batteries/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("batteryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/battery-service")))
                        .uri("http://battery-service:8085"))

                .build();
    }
}
