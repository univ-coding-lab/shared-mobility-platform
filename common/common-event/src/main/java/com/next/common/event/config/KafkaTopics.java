package com.next.common.event.config;

public final class KafkaTopics {

    public static final String VEHICLE_RENTED = "vehicle.rented";
    public static final String VEHICLE_RETURNED = "vehicle.returned";
    public static final String VEHICLE_MOVED = "vehicle.moved";
    public static final String VEHICLE_STATUS_CHANGED = "vehicle.status.changed";

    public static final String BATTERY_LOW = "battery.low";
    public static final String BATTERY_CRITICAL = "battery.critical";
    public static final String BATTERY_UPDATED = "battery.updated";

    public static final String LOCATION_UPDATED = "location.updated";

    public static final String RENTAL_STARTED = "rental.started";
    public static final String RENTAL_COMPLETED = "rental.completed";
    public static final String RENTAL_CANCELLED = "rental.cancelled";

    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String MAINTENANCE_REQUIRED = "maintenance.required";
    public static final String MAINTENANCE_COMPLETED = "maintenance.completed";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
