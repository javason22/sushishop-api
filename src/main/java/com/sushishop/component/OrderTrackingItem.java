package com.sushishop.component;

public class OrderTrackingItem {
    
    private Long orderId;
    private Long lastTrackTime;

    private Long timeSpent;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getLastTrackTime() {
        return lastTrackTime;
    }

    public void setLastTrackTime(Long startTime) {
        this.lastTrackTime = startTime;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

}
