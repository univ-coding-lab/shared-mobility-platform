package com.next.common.domain.enums;

/**
 * Vehicle operational status in the shared mobility system
 */
public enum VehicleStatus {
    /**
     * Vehicle is available for rental
     */
    AVAILABLE,

    /**
     * Vehicle is currently in use by a customer
     */
    IN_USE,

    /**
     * Vehicle is reserved but not yet in use
     */
    RESERVED,

    /**
     * Vehicle is undergoing maintenance or repairs
     */
    MAINTENANCE,

    /**
     * Vehicle is charging (for electric vehicles)
     */
    CHARGING,

    /**
     * Vehicle is out of service due to fault or damage
     */
    OUT_OF_SERVICE,

    /**
     * Vehicle is offline and cannot communicate
     */
    OFFLINE
}
