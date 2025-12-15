package com.next.batteryservice.service;

import com.next.common.domain.model.BatteryLog;
import com.next.common.event.publisher.EventPublisher;
import com.next.batteryservice.repository.BatteryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatteryServiceTest {

    @Mock
    private BatteryLogRepository batteryLogRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BatteryService batteryService;

    private BatteryLog batteryLog;

    @BeforeEach
    void setUp() {
        batteryLog = BatteryLog.builder()
                .vehicleId("vehicle-1")
                .batteryLevel(50)
                .estimatedRangeKm(25L)
                .timestamp(LocalDateTime.now())
                .build();
        batteryLog.setId("log-1");
    }

    @Test
    void saveBatteryLog_WithNormalLevel_ShouldSaveWithoutEvent() {

        batteryLog.setBatteryLevel(50);
        when(batteryLogRepository.save(any(BatteryLog.class))).thenReturn(batteryLog);

        BatteryLog result = batteryService.saveBatteryLog(batteryLog);

        assertNotNull(result);
        verify(batteryLogRepository).save(batteryLog);
        verify(eventPublisher, never()).publish(anyString(), anyString(), any());
    }

    @Test
    void saveBatteryLog_WithLowLevel_ShouldSaveAndPublishEvent() {

        batteryLog.setBatteryLevel(15);
        when(batteryLogRepository.save(any(BatteryLog.class))).thenReturn(batteryLog);

        BatteryLog result = batteryService.saveBatteryLog(batteryLog);

        assertNotNull(result);
        verify(batteryLogRepository).save(batteryLog);
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    void getLatestBatteryLog_ShouldReturnLatestLog() {

        when(batteryLogRepository.findTopByVehicleIdOrderByTimestampDesc("vehicle-1")).thenReturn(batteryLog);

        BatteryLog result = batteryService.getLatestBatteryLog("vehicle-1");

        assertNotNull(result);
        assertEquals("vehicle-1", result.getVehicleId());
        verify(batteryLogRepository).findTopByVehicleIdOrderByTimestampDesc("vehicle-1");
    }
}
