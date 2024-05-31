package com.sushishop;

public class Constant {
    
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

    // redis key names
    public static final String CACHE_PENDING_ORDERS = "pending-orders";
    public static final String CACHE_PROCESSING_ORDERS = "processing-orders";
    public static final String CACHE_PAUSED_ORDERS = "paused-orders";
    public static final String CACHE_CANCELLED_ORDERS = "cancelled-orders";
    public static final String CACHE_FINISHED_ORDERS = "finished-orders";
}
