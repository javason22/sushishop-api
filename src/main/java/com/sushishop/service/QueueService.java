package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.pojo.ChefOrder;
import jakarta.annotation.PostConstruct;
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

    public void initProcessors(int maxProcessors) {
        for(int i = 0; i < maxProcessors; i++) {
            redisTemplate.opsForList().leftPush("processing-orders", ChefOrder.builder().build());
        }
    }


    /**
     * push order to pending queue
     *
     * @param orderId order id
     * @param timeRequired time required to finish the order (in seconds)
     */
    public void pushOrderToPending(Long orderId, int timeRequired) {
        ChefOrder sOrder = ChefOrder.builder()
                .orderId(orderId)
                .progress(0L)
                .startAt(0L)
                .timeRequired(timeRequired * 1000L).build();
        redisTemplate.opsForList().leftPush("pending-orders", sOrder);
        log.info("Push order to pending-order: {}", sOrder);
        log.info("pending-orders after left Push {}", redisTemplate.opsForList().range("pending-orders", 0, -1));
    }

    public ChefOrder popOrderFromPending() {
        ChefOrder order =  (ChefOrder)redisTemplate.opsForList().rightPop("pending-orders");
        if(order != null) {
            log.info("Pop order from pending-order: {}", order);
            log.info("pending-orders after right Pop {}", redisTemplate.opsForList().range("pending-orders", 0, -1));
        }
        return order;
    }

    public Long removeOrderFromPending(Long orderId) {
        Long result = redisTemplate.opsForList().remove("pending-orders", 1,
                ChefOrder.builder().orderId(orderId).build());
        log.info("Remove order from pending-order: {}", orderId);
        log.info("pending-orders after remove {}", redisTemplate.opsForList().range("pending-orders", 0, -1));
        return result;
    }

    public void putOrderToProcessing(int index, ChefOrder order) {
        if(index >= redisTemplate.opsForList().size("processing-orders")) {
            throw new RuntimeException("Too many orders in processing");
        }
        redisTemplate.opsForList().set("processing-orders", index, order);
        log.info("Set order to processing-order: index: {} order: {}", index, order);
        log.info("processing-orders after set order {}", redisTemplate.opsForList().range("processing-orders", 0, -1));
    }

    public ChefOrder getOrderFromProcessing(int index) {

        ChefOrder order = (ChefOrder)redisTemplate.opsForList().index("processing-orders", index);
        log.info("Get order from processing-order: {}", order);
        log.info("processing-orders after get order {}", redisTemplate.opsForList().range("processing-orders", 0, -1));
        return order;
    }

    public void removeOrderFromProcessing(Long orderId) {
        redisTemplate.opsForList().remove("processing-orders", 1,
                ChefOrder.builder().orderId(orderId).build());
        log.info("Remove order from processing-order: {}", orderId);
        log.info("processing-orders after remove {}", redisTemplate.opsForList().range("processing-orders", 0, -1));
    }

    public ChefOrder getOrderFromProcessing(Long orderId, boolean remove) {
        ChefOrder order = (ChefOrder)redisTemplate.opsForList().range("processing-orders", 0, -1)
                .stream().filter(o -> ((ChefOrder)o).getOrderId().equals(orderId)).findFirst().orElse(null);
        if(remove) {
            removeOrderFromProcessing(orderId);
        }
        log.info("Get order from processing-order: {}", order);
        log.info("processing-orders after get {}", redisTemplate.opsForList().range("processing-orders", 0, -1));
        return order;
    }

    public boolean moveOrderFromProcessingToPausing(Long orderId) {
        // get order from processing and put it to pausing
        ChefOrder orderFromProcessing = getOrderFromProcessing(orderId, true);
        if(orderFromProcessing == null) {
            log.error("Order {} not found in processing", orderId);
            return false;
        }
        redisTemplate.opsForHash().put("pausing-orders", orderId, orderFromProcessing);
        log.info("Move order from processing to pausing: {}", orderFromProcessing);
        log.info("pausing-orders after put {}", redisTemplate.opsForHash().entries("pausing-orders"));
        return true;
    }

    public ChefOrder getOrderFromPausing(Long orderId, boolean remove) {
        ChefOrder order = (ChefOrder)redisTemplate.opsForHash().get("pausing-orders", orderId);
        if(remove) {
            removeOrderFromPausing(orderId);
        }
        log.info("Get order from pausing-orders: {}", order);
        log.info("pausing-orders after get {}", redisTemplate.opsForHash().entries("pausing-orders"));
        return order;
    }

    public void removeOrderFromPausing(Long orderId) {
        redisTemplate.opsForHash().delete("pausing-orders", orderId);
        log.info("Remove order from pausing-orders: {}", orderId);
        log.info("pausing-orders after remove {}", redisTemplate.opsForHash().entries("pausing-orders"));
    }

    public boolean moveOrderFromPausingToPending(Long orderId) {
        // get order from pausing and put it to pending
        ChefOrder orderFromPausing = getOrderFromPausing(orderId, true);
        if(orderFromPausing == null) {
            log.error("Order {} not found in pausing", orderId);
            return false;
        }
        orderFromPausing.setStartAt(Instant.now().toEpochMilli()); // reset start time
        // inject order to the beginning of the queue
        redisTemplate.opsForList().rightPush("pending-orders", orderId, orderFromPausing);
        log.info("Move order from pausing to pending: {}", orderFromPausing);
        log.info("pending-orders after right Push {}", redisTemplate.opsForList().range("pending-orders", 0, -1));
        return true;
    }

}
