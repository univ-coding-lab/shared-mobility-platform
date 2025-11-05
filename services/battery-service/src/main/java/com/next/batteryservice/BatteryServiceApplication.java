package com.next.batteryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.next.batteryservice", "com.next.common.event"})
@EnableMongoRepositories(basePackages = "com.next.batteryservice.repository")
@EnableKafka
public class BatteryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatteryServiceApplication.class, args);
    }
}
