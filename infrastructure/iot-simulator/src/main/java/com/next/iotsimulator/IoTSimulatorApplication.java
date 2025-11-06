package com.next.iotsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT Device Simulator Application
 * Simulates vehicle sensors, GPS tracking, and battery telemetry
 */
@SpringBootApplication
@EnableScheduling
public class IoTSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IoTSimulatorApplication.class, args);
    }
}
