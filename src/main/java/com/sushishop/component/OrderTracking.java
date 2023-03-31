package com.sushishop.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OrderTracking {
    /**
     * This is a spring component to track the time spent on making sushi. Since the pre-defined table
     * do not have field to store time spent and last update time of order in-progress, therefore it could 
     * be only done in memory.
     * 
     */
    private final Map<Long, OrderTrackingItem> orderTimeMap = new ConcurrentHashMap<>();

    public void trackOrderTime(Long orderId) {

        if(!orderTimeMap.containsKey(orderId)){
            OrderTrackingItem item = new OrderTrackingItem();
            item.setOrderId(orderId);
            item.setLastTrackTime(System.currentTimeMillis());
            item.setTimeSpent(0L);
            orderTimeMap.put(orderId, item);
        }else{
            Long currentTime = System.currentTimeMillis();
            Long timeSpent = orderTimeMap.get(orderId).getTimeSpent();
            timeSpent += (currentTime - orderTimeMap.get(orderId).getLastTrackTime());
            orderTimeMap.get(orderId).setTimeSpent(timeSpent);
            orderTimeMap.get(orderId).setLastTrackTime(currentTime);

            //Long timeSpent = (System.currentTimeMillis() - orderTimeMap.get(orderId).getStartTime()) / 1000;
            //orderTimeMap.get(orderId).setTimeSpent(timeSpent);
        }
    }


    public void resumeTracking(Long orderId){
        orderTimeMap.get(orderId).setLastTrackTime(System.currentTimeMillis());
    }

    public Long getTimeSpent(Long orderId) {
        return orderTimeMap.get(orderId).getTimeSpent() / 1000;
    }
}
