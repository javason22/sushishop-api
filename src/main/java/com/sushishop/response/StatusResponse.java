package com.sushishop.response;

public class StatusResponse {
    
    private Long orderId;

    private Long timeSpent;

    

    public StatusResponse(Long orderId, Long timeSpent) {
        this.orderId = orderId;
        this.timeSpent = timeSpent;
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

    
}
