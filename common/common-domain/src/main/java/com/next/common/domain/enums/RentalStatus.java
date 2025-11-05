package com.next.common.domain.enums;

/**
 * Status of a rental transaction
 */
public enum RentalStatus {
    /**
     * Rental has been created but not yet started
     */
    PENDING,

    /**
     * Rental is currently active
     */
    ACTIVE,

    /**
     * Rental has been paused by the user
     */
    PAUSED,

    /**
     * Rental has been completed successfully
     */
    COMPLETED,

    /**
     * Rental was cancelled before starting
     */
    CANCELLED,

    /**
     * Rental ended with issues (e.g., payment failure, vehicle damage)
     */
    FAILED
}
