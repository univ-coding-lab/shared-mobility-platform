package com.next.common.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validation operations
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$" // E.164 international format
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format (E.164)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate UUID format
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid.trim()).matches();
    }

    /**
     * Validate string is not null or blank
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Validate number is within range
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();
        return val >= minVal && val <= maxVal;
    }

    /**
     * Validate battery level (0-100)
     */
    public static boolean isValidBatteryLevel(Integer level) {
        return level != null && level >= 0 && level <= 100;
    }

    /**
     * Validate latitude (-90 to 90)
     */
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validate longitude (-180 to 180)
     */
    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Validate GPS coordinates
     */
    public static boolean isValidCoordinates(Double latitude, Double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }
}
