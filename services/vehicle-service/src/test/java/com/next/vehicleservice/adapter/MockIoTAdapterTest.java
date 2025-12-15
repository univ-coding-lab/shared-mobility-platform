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

        int batteryLevel = mockIoTAdapter.getBatteryLevel("device-123");

        assertEquals(75, batteryLevel);
    }

    @Test
    void getLocation_ShouldReturnSeoulCoordinates() {

        Location location = mockIoTAdapter.getLocation("device-123");

        assertNotNull(location);
        assertEquals("device-123", location.getVehicleId());
        assertEquals(37.5665, location.getLatitude());
        assertEquals(126.9780, location.getLongitude());
    }

    @Test
    void lockVehicle_ShouldExecuteWithoutError() {

        assertDoesNotThrow(() -> mockIoTAdapter.lockVehicle("device-123"));
    }

    @Test
    void unlockVehicle_ShouldExecuteWithoutError() {

        assertDoesNotThrow(() -> mockIoTAdapter.unlockVehicle("device-123"));
    }
}
