package com.sushishop;

public final class Constant {
    
    // all the order statuses
    public static final String STATUS_CREATED = "created";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_FINISHED = "finished";
    public static final String STATUS_CANCELLED = "cancelled";

    // number of chefs
    public static final int MAX_CHEF = 3;
    // time interval for the fix rate hazelcast scheduler
    public static final int SCHEDULER_INTERVAL = 1000;
}
