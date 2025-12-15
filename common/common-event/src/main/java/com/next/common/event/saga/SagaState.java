package com.next.common.event.saga;

public enum SagaState {
    STARTED("Saga started"),
    VEHICLE_RESERVED("Vehicle reserved"),
    PAYMENT_COMPLETED("Payment completed"),
    VEHICLE_UNLOCKED("Vehicle unlocked"),
    LOCATION_TRACKING_STARTED("Location tracking started"),
    COMPLETED("Saga completed successfully"),

    COMPENSATING("Compensating transaction in progress"),
    PAYMENT_REFUNDED("Payment refunded"),
    VEHICLE_RELEASED("Vehicle released from reservation"),
    FAILED("Saga failed");

    private final String description;

    SagaState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompensating() {
        return this == COMPENSATING || this == PAYMENT_REFUNDED || this == VEHICLE_RELEASED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
