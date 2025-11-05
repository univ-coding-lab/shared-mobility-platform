package com.next.rentalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.next.rentalservice", "com.next.common.event"})
@EntityScan(basePackages = "com.next.common.domain.model")
@EnableJpaAuditing
@EnableKafka
public class RentalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalServiceApplication.class, args);
    }
}
