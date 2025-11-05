package com.next.vehicleservice.adapter;

import com.next.common.domain.model.Location;
import com.next.vehicleservice.port.IoTDevicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component("mockAdapter")
@Primary
public class MockIoTAdapter implements IoTDevicePort {
    @Override
    public int getBatteryLevel(String deviceId) {
        log.info("MOCK: Getting battery for device {}", deviceId);
        return 75;
    }

    @Override
    public Location getLocation(String deviceId) {
        log.info("MOCK: Getting location for device {}", deviceId);
        return Location.of(deviceId, 37.5665, 126.9780);
    }

    @Override
    public void lockVehicle(String deviceId) {
        log.info("MOCK: Locking device {}", deviceId);
    }

    @Override
    public void unlockVehicle(String deviceId) {
        log.info("MOCK: Unlocking device {}", deviceId);
    }
}
