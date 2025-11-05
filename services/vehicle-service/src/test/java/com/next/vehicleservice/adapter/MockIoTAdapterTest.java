package com.next.vehicleservice.adapter;

import com.next.common.domain.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockIoTAdapterTest {

    private MockIoTAdapter mockIoTAdapter;

    @BeforeEach
    void setUp() {
        mockIoTAdapter = new MockIoTAdapter();
    }

    @Test
    void getBatteryLevel_ShouldReturnFixedValue() {
        // when
        int batteryLevel = mockIoTAdapter.getBatteryLevel("device-123");

        // then
        assertEquals(75, batteryLevel);
    }

    @Test
    void getLocation_ShouldReturnSeoulCoordinates() {
        // when
        Location location = mockIoTAdapter.getLocation("device-123");

        // then
        assertNotNull(location);
        assertEquals("device-123", location.getVehicleId());
        assertEquals(37.5665, location.getLatitude());
        assertEquals(126.9780, location.getLongitude());
    }

    @Test
    void lockVehicle_ShouldExecuteWithoutError() {
        // when & then - should not throw exception
        assertDoesNotThrow(() -> mockIoTAdapter.lockVehicle("device-123"));
    }

    @Test
    void unlockVehicle_ShouldExecuteWithoutError() {
        // when & then - should not throw exception
        assertDoesNotThrow(() -> mockIoTAdapter.unlockVehicle("device-123"));
    }
}
