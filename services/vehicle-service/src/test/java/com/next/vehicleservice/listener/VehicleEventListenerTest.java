package com.next.vehicleservice.listener;

import com.next.common.domain.enums.VehicleStatus;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.vehicleservice.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleEventListenerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private IdempotencyChecker idempotencyChecker;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private VehicleEventListener vehicleEventListener;

    private VehicleRentedEvent vehicleRentedEvent;
    private VehicleReturnedEvent vehicleReturnedEvent;

    @BeforeEach
    void setUp() {
        vehicleRentedEvent = VehicleRentedEvent.builder()
                .eventId("event-123")
                .vehicleId("vehicle-1")
                .userId("user-1")
                .rentalId("rental-1")
                .startLatitude(37.5665)
                .startLongitude(126.9780)
                .batteryLevel(85)
                .build();

        vehicleReturnedEvent = VehicleReturnedEvent.builder()
                .eventId("event-456")
                .vehicleId("vehicle-1")
                .userId("user-1")
                .rentalId("rental-1")
                .endLatitude(37.5700)
                .endLongitude(126.9800)
                .batteryLevel(65)
                .distanceKm(5.2)
                .durationMinutes(30L)
                .build();
    }

    @Test
    void handleVehicleRented_Success() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doNothing().when(vehicleService).updateVehicleStatus(any(), any());

        vehicleEventListener.handleVehicleRented(vehicleRentedEvent, 0, 100L, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-123");
        verify(vehicleService).updateVehicleStatus("vehicle-1", VehicleStatus.IN_USE);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleRented_DuplicateEvent_ShouldSkip() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(false);

        vehicleEventListener.handleVehicleRented(vehicleRentedEvent, 0, 100L, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-123");
        verify(vehicleService, never()).updateVehicleStatus(any(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleRented_ServiceException_ShouldNotAcknowledge() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(vehicleService).updateVehicleStatus(any(), any());

        try {
            vehicleEventListener.handleVehicleRented(vehicleRentedEvent, 0, 100L, acknowledgment);
        } catch (RuntimeException e) {

        }

        verify(vehicleService).updateVehicleStatus("vehicle-1", VehicleStatus.IN_USE);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void handleVehicleReturned_Success() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doNothing().when(vehicleService).updateVehicleStatus(any(), any());

        vehicleEventListener.handleVehicleReturned(vehicleReturnedEvent, 0, 100L, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-456");
        verify(vehicleService).updateVehicleStatus("vehicle-1", VehicleStatus.AVAILABLE);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleReturned_DuplicateEvent_ShouldSkip() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(false);

        vehicleEventListener.handleVehicleReturned(vehicleReturnedEvent, 0, 100L, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-456");
        verify(vehicleService, never()).updateVehicleStatus(any(), any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleReturned_ServiceException_ShouldNotAcknowledge() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(vehicleService).updateVehicleStatus(any(), any());

        try {
            vehicleEventListener.handleVehicleReturned(vehicleReturnedEvent, 0, 100L, acknowledgment);
        } catch (RuntimeException e) {

        }

        verify(vehicleService).updateVehicleStatus("vehicle-1", VehicleStatus.AVAILABLE);
        verify(acknowledgment, never()).acknowledge();
    }
}
