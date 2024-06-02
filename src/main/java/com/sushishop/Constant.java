package com.sushishop;

public class Constant {

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
