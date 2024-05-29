package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.pojo.StatefulOrder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
@Service
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * push order to pending queue
     *
     * @param orderId order id
     * @param timeRequired time required to finish the order (in seconds)
     */
    public void pushOrderToPending(Long orderId, int timeRequired) {
        StatefulOrder sOrder = StatefulOrder.builder()
                .orderId(orderId)
                .progress(0L)
                .startAt(0L)
                .timeRequired(timeRequired * 1000L).build();
        redisTemplate.opsForList().leftPush("pending-orders", sOrder);
    }

    public StatefulOrder popOrderFromPending() {
        return (StatefulOrder)redisTemplate.opsForList().rightPop("pending-orders");
    }

    public Long removeOrderFromPending(Long orderId) {
        return redisTemplate.opsForList().remove("pending-orders", 1,
                StatefulOrder.builder().orderId(orderId).build());
    }

    public void putOrderToProcessing(StatefulOrder order) {
        if(redisTemplate.opsForList().size("processing-orders") >= Constant.MAX_CHEF) {
            throw new RuntimeException("Too many orders in processing");
        }
        redisTemplate.opsForList().leftPush("processing-orders", order.getOrderId(), order);
    }

    public StatefulOrder getOrderFromProcessing(int index) {
        return (StatefulOrder)redisTemplate.opsForList().index("processing-orders", index);
    }

    public void removeOrderFromProcessing(Long orderId) {
        redisTemplate.opsForList().remove("processing-orders", 1,
                StatefulOrder.builder().orderId(orderId).build());
    }

    public StatefulOrder getOrderFromProcessing(Long orderId, boolean remove) {
        StatefulOrder order = (StatefulOrder)redisTemplate.opsForList().range("processing-orders", 0, -1)
                .stream().filter(o -> ((StatefulOrder)o).getOrderId().equals(orderId)).findFirst().orElse(null);
        if(remove) {
            removeOrderFromProcessing(orderId);
        }
        return order;
    }

    public boolean moveOrderFromProcessingToPausing(Long orderId) {
        // get order from processing and put it to pausing
        StatefulOrder orderFromProcessing = getOrderFromProcessing(orderId, true);
        if(orderFromProcessing == null) {
            log.error("Order {} not found in processing", orderId);
            return false;
        }
        redisTemplate.opsForHash().put("pausing-orders", orderId, orderFromProcessing);
        return true;
    }

    public StatefulOrder getOrderFromPausing(Long orderId, boolean remove) {
        StatefulOrder order = (StatefulOrder)redisTemplate.opsForHash().get("pausing-orders", orderId);
        if(remove) {
            removeOrderFromPausing(orderId);
        }
        return order;
    }

    public void removeOrderFromPausing(Long orderId) {
        redisTemplate.opsForHash().delete("pausing-orders", orderId);
    }

    public boolean moveOrderFromPausingToPending(Long orderId) {
        // get order from pausing and put it to pending
        StatefulOrder orderFromPausing = getOrderFromPausing(orderId, true);
        if(orderFromPausing == null) {
            log.error("Order {} not found in pausing", orderId);
            return false;
        }
        orderFromPausing.setStartAt(Instant.now().toEpochMilli()); // reset start time
        // inject order to the beginning of the queue
        redisTemplate.opsForList().rightPush("pending-orders", orderId, orderFromPausing);
        return true;
    }

}
