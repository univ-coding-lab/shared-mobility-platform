package com.next.common.domain.constants;

public final class BatteryConstants {

    private BatteryConstants() {
    }

    public static final int CRITICALLY_LOW_THRESHOLD = 10;
    public static final int NEEDS_CHARGING_THRESHOLD = 20;
    public static final int MINIMUM_FOR_RENTAL = 20;

    public static final int HEALTH_GOOD_THRESHOLD = 80;
    public static final int HEALTH_FAIR_THRESHOLD = 50;
    public static final int HEALTH_POOR_THRESHOLD = 20;

    public static final int RANGE_ESTIMATION_FACTOR = 2;
}
