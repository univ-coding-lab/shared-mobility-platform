package com.next.vehicleservice.listener;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.event.config.KafkaTopics;
import com.next.common.event.consumer.AbstractEventListener;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.DomainEvent;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.vehicleservice.service.VehicleService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventListener extends AbstractEventListener<DomainEvent> {

    private final VehicleService vehicleService;

    public VehicleEventListener(IdempotencyChecker idempotencyChecker, VehicleService vehicleService) {
        super(idempotencyChecker);
        this.vehicleService = vehicleService;
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RENTED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleRented(@Payload VehicleRentedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleRented", this::processVehicleRented);
    }

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RETURNED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleVehicleReturned(@Payload VehicleReturnedEvent event, Acknowledgment acknowledgment) {
        handleEventWithProcessor(event, acknowledgment, "VehicleReturned", this::processVehicleReturned);
    }

    private void processVehicleRented(VehicleRentedEvent event) {
        vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.IN_USE);
    }

    private void processVehicleReturned(VehicleReturnedEvent event) {
        vehicleService.updateVehicleStatus(event.getVehicleId(), VehicleStatus.AVAILABLE);
    }
}
