package com.sushishop.component;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

import jakarta.annotation.PostConstruct;

@Component
public class OrderTracking {
    /**
     * This is a spring component to track the time spent on making sushi. Since the pre-defined table
     * do not have field to store time spent and last update time of order in-progress, therefore it could 
     * be only done in memory.
     * 
     */
    //private final Map<Long, OrderTrackingItem> orderTimeMap = new ConcurrentHashMap<>();
    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void trackOrderTime(Long orderId) {

        Map<Long, OrderTrackingItem> orderTimeMap = hazelcastInstance.getMap("tracking");

        if(!orderTimeMap.containsKey(orderId)){
            OrderTrackingItem item = new OrderTrackingItem(
                orderId, System.currentTimeMillis(), 0L);
            orderTimeMap.put(orderId, item);
        }else{
            Long currentTime = System.currentTimeMillis();
            Long timeSpent = orderTimeMap.get(orderId).getTimeSpent();
            timeSpent += (currentTime - orderTimeMap.get(orderId).getLastTrackTime());

            OrderTrackingItem item = orderTimeMap.get(orderId);
            item.setTimeSpent(timeSpent);
            item.setLastTrackTime(currentTime);
            // put it back to cache
            orderTimeMap.put(orderId, item);

            //Long timeSpent = (System.currentTimeMillis() - orderTimeMap.get(orderId).getStartTime()) / 1000;
            //orderTimeMap.get(orderId).setTimeSpent(timeSpent);
        }
    }


    public void resumeTracking(Long orderId){
        Map<Long, OrderTrackingItem> orderTimeMap = hazelcastInstance.getMap("tracking");
        OrderTrackingItem item = orderTimeMap.get(orderId);
        item.setLastTrackTime(System.currentTimeMillis());
        orderTimeMap.put(orderId, item);
    }

    public Long getTimeSpent(Long orderId) {
        Map<Long, OrderTrackingItem> orderTimeMap = hazelcastInstance.getMap("tracking");
        return orderTimeMap.get(orderId).getTimeSpent() / 1000;
    }

    @PostConstruct
    public void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hazelcastInstance.shutdown();
        }));
    }
}
