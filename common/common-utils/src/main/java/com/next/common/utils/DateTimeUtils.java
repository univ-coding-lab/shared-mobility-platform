package com.next.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Format LocalDateTime to ISO string
     */
    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }

    /**
     * Format LocalDateTime for display
     */
    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Parse ISO string to LocalDateTime
     */
    public static LocalDateTime parseISO(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
    }

    /**
     * Calculate duration between two timestamps in minutes
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Calculate duration between two timestamps in hours
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toHours();
    }

    /**
     * Check if timestamp is within the past N minutes
     */
    public static boolean isWithinPastMinutes(LocalDateTime timestamp, long minutes) {
        if (timestamp == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return timestamp.isAfter(threshold);
    }

    /**
     * Check if timestamp is in the future
     */
    public static boolean isFuture(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        return timestamp.isAfter(LocalDateTime.now());
    }

    /**
     * Get current timestamp
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
