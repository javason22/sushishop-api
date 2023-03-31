package com.sushishop.response;

import com.sushishop.entity.SushiOrder;

public class OrderResponse extends BaseResponse{
    
    private SushiOrder order;

    public OrderResponse(int code, String msg, SushiOrder order) {
        super(code, msg);
        this.order = order;
    }

    public SushiOrder getOrder() {
        return order;
    }

    public void setOrder(SushiOrder order) {
        this.order = order;
    }

    
}
