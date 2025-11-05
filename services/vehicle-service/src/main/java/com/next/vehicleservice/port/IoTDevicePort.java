package com.next.vehicleservice.port;

import com.next.common.domain.model.Location;

public interface IoTDevicePort {
    int getBatteryLevel(String deviceId);
    Location getLocation(String deviceId);
    void lockVehicle(String deviceId);
    void unlockVehicle(String deviceId);
}
