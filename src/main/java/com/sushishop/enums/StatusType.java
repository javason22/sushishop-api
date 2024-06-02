package com.sushishop.enums;

import com.sushishop.Constant;
import lombok.Getter;

@Getter
public enum StatusType {
    CREATED("created", Constant.CACHE_PENDING_ORDERS),
    IN_PROGRESS("in-progress", Constant.CACHE_PROCESSING_ORDERS),
    PAUSED("paused", Constant.CACHE_PAUSED_ORDERS),
    FINISHED("finished", Constant.CACHE_FINISHED_ORDERS),
    CANCELLED("cancelled", Constant.CACHE_CANCELLED_ORDERS);

    final String status;
    final String queueName;

    StatusType(String status, String queueName) {
        this.status = status;
        this.queueName = queueName;
    }

    public static StatusType getFromString(String status) {
        for (StatusType s : StatusType.values()) {
            if (s.status.equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }
}
