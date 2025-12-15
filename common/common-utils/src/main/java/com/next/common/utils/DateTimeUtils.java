package com.next.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public static LocalDateTime parseISO(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toHours();
    }

    public static boolean isWithinPastMinutes(LocalDateTime timestamp, long minutes) {
        if (timestamp == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return timestamp.isAfter(threshold);
    }

    public static boolean isFuture(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        return timestamp.isAfter(LocalDateTime.now());
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
