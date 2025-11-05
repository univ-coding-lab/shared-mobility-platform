package com.next.vehicleservice.adapter;

import com.next.common.domain.model.Location;
import com.next.vehicleservice.port.IoTDevicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("httpAdapter")
public class HttpIoTAdapter implements IoTDevicePort {
    @Override
    public int getBatteryLevel(String deviceId) {
        log.info("HTTP: Getting battery for device {}", deviceId);
        return 85;
    }

    @Override
    public Location getLocation(String deviceId) {
        log.info("HTTP: Getting location for device {}", deviceId);
        return Location.of(deviceId, 37.5665, 126.9780);
    }

    @Override
    public void lockVehicle(String deviceId) {
        log.info("HTTP: Locking device {}", deviceId);
    }

    @Override
    public void unlockVehicle(String deviceId) {
        log.info("HTTP: Unlocking device {}", deviceId);
    }
}
