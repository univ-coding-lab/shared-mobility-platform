package com.next.locationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.next.locationservice", "com.next.common.event"})
@EnableMongoRepositories(basePackages = "com.next.locationservice.repository")
@EnableKafka
public class LocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
