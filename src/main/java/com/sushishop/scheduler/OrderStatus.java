package com.sushishop.scheduler;

public class OrderStatus {
    
    private Long orderId; 

    private Long lastUpdateTime;
    
    private Long timeSpent;

    private String status;

    public OrderStatus(Long orderId, Long timeSpent, String status, Long lastUpdateLong) {
        this.orderId = orderId;
        this.timeSpent = timeSpent;
        this.status = status;
        this.lastUpdateTime = lastUpdateLong;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "OrderStatus [orderId=" + orderId + ", lastUpdateTime=" + lastUpdateTime + ", timeSpent=" + timeSpent
                + ", status=" + status + "]";
    }

    
}
