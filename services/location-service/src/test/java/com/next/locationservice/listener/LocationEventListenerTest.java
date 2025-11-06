package com.next.locationservice.listener;

import com.next.common.domain.model.Location;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleMovedEvent;
import com.next.common.event.model.VehicleRentedEvent;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.locationservice.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocationEventListener
 */
@ExtendWith(MockitoExtension.class)
class LocationEventListenerTest {

    @Mock
    private LocationService locationService;

    @Mock
    private IdempotencyChecker idempotencyChecker;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private LocationEventListener locationEventListener;

    private VehicleRentedEvent vehicleRentedEvent;
    private VehicleReturnedEvent vehicleReturnedEvent;
    private VehicleMovedEvent vehicleMovedEvent;

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

        vehicleMovedEvent = VehicleMovedEvent.builder()
                .eventId("event-789")
                .vehicleId("vehicle-1")
                .latitude(37.5680)
                .longitude(126.9790)
                .speed(25.5)
                .heading(90.0)
                .isMoving(true)
                .rentalId("rental-1")
                .build();
    }

    @Test
    void handleVehicleRented_Success() {
        // Given
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        // When
        locationEventListener.handleVehicleRented(vehicleRentedEvent, 0, 100L, acknowledgment);

        // Then
        verify(idempotencyChecker).processIdempotently("event-123");
        verify(locationService).saveLocation(locationCaptor.capture());
        verify(acknowledgment).acknowledge();

        Location savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getVehicleId()).isEqualTo("vehicle-1");
        assertThat(savedLocation.getLatitude()).isEqualTo(37.5665);
        assertThat(savedLocation.getLongitude()).isEqualTo(126.9780);
        assertThat(savedLocation.getRentalId()).isEqualTo("rental-1");
        assertThat(savedLocation.getSource()).isEqualTo("RENTAL_START");
    }

    @Test
    void handleVehicleReturned_Success() {
        // Given
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        // When
        locationEventListener.handleVehicleReturned(vehicleReturnedEvent, 0, 100L, acknowledgment);

        // Then
        verify(idempotencyChecker).processIdempotently("event-456");
        verify(locationService).saveLocation(locationCaptor.capture());
        verify(acknowledgment).acknowledge();

        Location savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getVehicleId()).isEqualTo("vehicle-1");
        assertThat(savedLocation.getLatitude()).isEqualTo(37.5700);
        assertThat(savedLocation.getLongitude()).isEqualTo(126.9800);
        assertThat(savedLocation.getRentalId()).isEqualTo("rental-1");
        assertThat(savedLocation.getSource()).isEqualTo("RENTAL_END");
    }

    @Test
    void handleVehicleMoved_Success() {
        // Given
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        // When
        locationEventListener.handleVehicleMoved(vehicleMovedEvent, 0, 100L, acknowledgment);

        // Then
        verify(idempotencyChecker).processIdempotently("event-789");
        verify(locationService).saveLocation(locationCaptor.capture());
        verify(acknowledgment).acknowledge();

        Location savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getVehicleId()).isEqualTo("vehicle-1");
        assertThat(savedLocation.getLatitude()).isEqualTo(37.5680);
        assertThat(savedLocation.getLongitude()).isEqualTo(126.9790);
        assertThat(savedLocation.getSpeed()).isEqualTo(25.5);
        assertThat(savedLocation.getHeading()).isEqualTo(90.0);
        assertThat(savedLocation.getIsMoving()).isTrue();
        assertThat(savedLocation.getSource()).isEqualTo("GPS");
    }

    @Test
    void handleVehicleRented_DuplicateEvent_ShouldSkip() {
        // Given
        when(idempotencyChecker.processIdempotently(any())).thenReturn(false);

        // When
        locationEventListener.handleVehicleRented(vehicleRentedEvent, 0, 100L, acknowledgment);

        // Then
        verify(locationService, never()).saveLocation(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleMoved_ServiceException_ShouldNotAcknowledge() {
        // Given
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(locationService).saveLocation(any());

        // When & Then
        try {
            locationEventListener.handleVehicleMoved(vehicleMovedEvent, 0, 100L, acknowledgment);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(acknowledgment, never()).acknowledge();
    }
}
