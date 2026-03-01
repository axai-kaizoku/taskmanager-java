package com.example.taskmanager.util;

public final class AppConstants {

    // Private constructor to prevent instantiation
    private AppConstants() {}

    // Time in Milliseconds
    public static final long ONE_MINUTE_MS = 60_000L;
    public static final long FIVE_MINUTES_MS = 300_000L;
    public static final long TEN_MINUTES_MS = 600_000L;
    public static final long TWENTY_MINUTES_MS = 1_200_000L;
    public static final long ONE_HOUR_MS = 3_600_000L;
    public static final long ONE_DAY_MS = 86_400_000L;

    // Cache Keys Prefix (Optional but recommended)
    public static final String CACHE_TASK_PREFIX = "task:";
    public static final String CACHE_USER_PREFIX = "user:";
}
