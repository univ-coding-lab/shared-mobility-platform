package com.next.batteryservice.listener;

import com.next.common.domain.model.BatteryLog;
import com.next.common.event.consumer.IdempotencyChecker;
import com.next.common.event.model.VehicleReturnedEvent;
import com.next.batteryservice.service.BatteryService;
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

@ExtendWith(MockitoExtension.class)
class BatteryEventListenerTest {

    @Mock
    private BatteryService batteryService;

    @Mock
    private IdempotencyChecker idempotencyChecker;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private BatteryEventListener batteryEventListener;

    private VehicleReturnedEvent vehicleReturnedEvent;

    @BeforeEach
    void setUp() {
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
    void handleVehicleReturned_Success() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<BatteryLog> batteryLogCaptor = ArgumentCaptor.forClass(BatteryLog.class);

        batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-456");
        verify(batteryService).saveBatteryLog(batteryLogCaptor.capture());
        verify(acknowledgment).acknowledge();

        BatteryLog savedLog = batteryLogCaptor.getValue();
        assertThat(savedLog.getVehicleId()).isEqualTo("vehicle-1");
        assertThat(savedLog.getBatteryLevel()).isEqualTo(65);
        assertThat(savedLog.getSource()).isEqualTo("RENTAL_END");
        assertThat(savedLog.getEstimatedRangeKm()).isEqualTo(130L);
        assertThat(savedLog.getHealthStatus()).isEqualTo("FAIR");
    }

    @Test
    void handleVehicleReturned_LowBattery_HealthStatusPoor() {

        vehicleReturnedEvent.setBatteryLevel(35);
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<BatteryLog> batteryLogCaptor = ArgumentCaptor.forClass(BatteryLog.class);

        batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);

        verify(batteryService).saveBatteryLog(batteryLogCaptor.capture());

        BatteryLog savedLog = batteryLogCaptor.getValue();
        assertThat(savedLog.getHealthStatus()).isEqualTo("POOR");
        assertThat(savedLog.getEstimatedRangeKm()).isEqualTo(70L);
    }

    @Test
    void handleVehicleReturned_CriticalBattery_HealthStatusCritical() {

        vehicleReturnedEvent.setBatteryLevel(15);
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<BatteryLog> batteryLogCaptor = ArgumentCaptor.forClass(BatteryLog.class);

        batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);

        verify(batteryService).saveBatteryLog(batteryLogCaptor.capture());

        BatteryLog savedLog = batteryLogCaptor.getValue();
        assertThat(savedLog.getHealthStatus()).isEqualTo("CRITICAL");
        assertThat(savedLog.getEstimatedRangeKm()).isEqualTo(30L);

    }

    @Test
    void handleVehicleReturned_HighBattery_HealthStatusGood() {

        vehicleReturnedEvent.setBatteryLevel(85);
        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        ArgumentCaptor<BatteryLog> batteryLogCaptor = ArgumentCaptor.forClass(BatteryLog.class);

        batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);

        verify(batteryService).saveBatteryLog(batteryLogCaptor.capture());

        BatteryLog savedLog = batteryLogCaptor.getValue();
        assertThat(savedLog.getHealthStatus()).isEqualTo("GOOD");
        assertThat(savedLog.getEstimatedRangeKm()).isEqualTo(170L);
    }

    @Test
    void handleVehicleReturned_DuplicateEvent_ShouldSkip() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(false);

        batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);

        verify(idempotencyChecker).processIdempotently("event-456");
        verify(batteryService, never()).saveBatteryLog(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleVehicleReturned_ServiceException_ShouldNotAcknowledge() {

        when(idempotencyChecker.processIdempotently(any())).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(batteryService).saveBatteryLog(any());

        try {
            batteryEventListener.handleVehicleReturned(vehicleReturnedEvent, acknowledgment);
        } catch (RuntimeException e) {

        }

        verify(batteryService).saveBatteryLog(any());
        verify(acknowledgment, never()).acknowledge();
    }
}
