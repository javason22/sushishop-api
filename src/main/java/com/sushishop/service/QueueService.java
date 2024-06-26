package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.pojo.ChefOrder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void initProcessors(int maxProcessors) {
        for(int i = 0; i < maxProcessors; i++) {
            redisTemplate.opsForList().leftPush(Constant.CACHE_PROCESSING_ORDERS, ChefOrder.builder().build());
        }
    }


    /**
     * push order to the pending queue
     *
     * @param orderId order id
     * @param timeRequired time required to finish the order (in seconds)
     */
    public void pushOrderToPending(Long orderId, int timeRequired) {
        ChefOrder sOrder = ChefOrder.builder()
                .orderId(orderId)
                .progress(0L)
                .lastUpdatedAt(0L)
                .timeRequired(timeRequired * 1000L).build();
        pushToList(Constant.CACHE_PENDING_ORDERS, sOrder);
    }

    public ChefOrder popOrderFromPending() {
        ChefOrder order =  (ChefOrder)redisTemplate.opsForList().rightPop(Constant.CACHE_PENDING_ORDERS);
        if(order != null) {
            log.info("Pop order from pending-order: {}", order);
            log.info("pending-orders after right Pop {}", redisTemplate.opsForList().range(Constant.CACHE_PENDING_ORDERS, 0, -1));
        }
        return order;
    }

    public void removeOrderFromPending(Long orderId) {
        redisTemplate.opsForList().remove(Constant.CACHE_PENDING_ORDERS, 1,
                ChefOrder.builder().orderId(orderId).build());
        log.info("Remove order from pending-order: {}", orderId);
        log.info("pending-orders after remove {}", redisTemplate.opsForList().range(Constant.CACHE_PENDING_ORDERS, 0, -1));
    }

    public void putOrderToProcessing(int index, ChefOrder order) {
        Long size = redisTemplate.opsForList().size(Constant.CACHE_PROCESSING_ORDERS);
        assert size != null : "Processing orders is null";
        if(index >= size) {
            throw new RuntimeException("Too many orders in processing");
        }
        redisTemplate.opsForList().set(Constant.CACHE_PROCESSING_ORDERS, index, order);
        log.info("Set order to processing-order: index: {} order: {}", index, order);
        log.info("processing-orders after set order {}", redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1));
    }

    public ChefOrder getOrderFromProcessing(int index) {

        ChefOrder order = (ChefOrder)redisTemplate.opsForList().index(Constant.CACHE_PROCESSING_ORDERS, index);
        log.info("processing-orders after get order : {} is {}", order, redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1));
        return order;
    }

    public void removeOrderFromProcessing(Long orderId) {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1);
        assert orders != null : "Processing orders is null";
        long index = orders.indexOf(ChefOrder.builder().orderId(orderId).build());
        if(index < 0){
            log.error("Order {} not found in processing", orderId);
            return;
        }
        redisTemplate.opsForList().set(Constant.CACHE_PROCESSING_ORDERS, index, ChefOrder.builder().build()); // reset the order
        log.info("processing-orders after removing order ID: {} is {}", orderId, redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1));
    }

    public ChefOrder getOrderFromProcessing(Long orderId, boolean remove) {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1);
        assert orders != null : "Processing orders is null";
        ChefOrder order = (ChefOrder) orders.stream().filter(o -> ((ChefOrder)o).getOrderId().equals(orderId)).findFirst().orElse(null);
        if(remove) {
            removeOrderFromProcessing(orderId);
        }
        log.info("Get order from processing-order: {}", order);
        log.info("processing-orders after get {}", redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1));
        return order;
    }

    public boolean moveOrderFromProcessingToPausing(Long orderId) {
        // get order from processing and put it to pausing
        ChefOrder orderFromProcessing = getOrderFromProcessing(orderId, true);
        if(orderFromProcessing == null) {
            log.error("Order {} not found in processing", orderId);
            return false;
        }
        redisTemplate.opsForHash().put(Constant.CACHE_PAUSED_ORDERS, String.valueOf(orderId), orderFromProcessing);
        log.info("Move order from processing to pausing: {}", orderFromProcessing);
        log.info("pausing-orders after put {}", redisTemplate.opsForHash().entries(Constant.CACHE_PAUSED_ORDERS));
        return true;
    }

    public ChefOrder getOrderFromPausing(Long orderId, boolean remove) {
        ChefOrder order = (ChefOrder)redisTemplate.opsForHash().get(Constant.CACHE_PAUSED_ORDERS, String.valueOf(orderId));
        if(remove) {
            removeOrderFromPausing(orderId);
        }
        log.info("Get order from pausing-orders: {}", order);
        log.info("pausing-orders after get {}", redisTemplate.opsForHash().entries(Constant.CACHE_PAUSED_ORDERS));
        return order;
    }

    public void removeOrderFromPausing(Long orderId) {
        redisTemplate.opsForHash().delete(Constant.CACHE_PAUSED_ORDERS, String.valueOf(orderId));
        log.info("Remove order from pausing-orders: {}", orderId);
        log.info("pausing-orders after remove {}", redisTemplate.opsForHash().entries(Constant.CACHE_PAUSED_ORDERS));
    }

    public boolean moveOrderFromPausingToPending(Long orderId) {
        // get order from pausing and put it to pending
        ChefOrder orderFromPausing = getOrderFromPausing(orderId, true);
        if(orderFromPausing == null) {
            log.error("Order {} not found in pausing", orderId);
            return false;
        }
        orderFromPausing.setLastUpdatedAt(Instant.now().toEpochMilli()); // reset start time
        // inject order to the beginning of the queue
        redisTemplate.opsForList().rightPush(Constant.CACHE_PENDING_ORDERS, orderFromPausing);
        log.info("Move order from pausing to pending: {}", orderFromPausing);
        log.info("pending-orders after right Push {}", redisTemplate.opsForList().range(Constant.CACHE_PENDING_ORDERS, 0, -1));
        return true;
    }

    public List<ChefOrder> getPendingOrders() {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_PENDING_ORDERS, 0, -1);
        assert orders != null : "Pending orders is null";
        return orders.stream().map(o -> (ChefOrder)o).toList();
    }

    public List<ChefOrder> getProcessingOrders() {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_PROCESSING_ORDERS, 0, -1);
        assert orders != null : "Processing orders is null";
        return orders.stream().map(o -> (ChefOrder)o).toList();
    }

    public List<ChefOrder> getPausingOrders() {
        return redisTemplate.opsForHash().values(Constant.CACHE_PAUSED_ORDERS)
                .stream().map(o -> (ChefOrder)o).toList();
    }

    public List<ChefOrder> getFinishedOrders() {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_FINISHED_ORDERS, 0, -1);
        assert orders != null : "Finished orders is null";
        return orders.stream().map(o -> (ChefOrder)o).toList();
    }

    public List<ChefOrder> getCancelledOrders() {
        List<Object> orders = redisTemplate.opsForList().range(Constant.CACHE_CANCELLED_ORDERS, 0, -1);
        assert orders != null : "Cancelled orders is null";
        return orders.stream().map(o -> (ChefOrder)o).toList();
    }

    public void pushOrderToFinished(ChefOrder order) {
        pushToList(Constant.CACHE_FINISHED_ORDERS, order);
    }

    public void pushOrderToCancelled(ChefOrder order) {
        pushToList(Constant.CACHE_CANCELLED_ORDERS, order);
    }

    private void pushToList(String key, ChefOrder order) {
        redisTemplate.opsForList().leftPush(key, order);
        log.info("Push order to {}: {}", key, order);
        log.info("{} after left Push {}", key, redisTemplate.opsForList().range(key, 0, -1));
    }

    public void moveOrderFromPendingToCancel(Long orderId) {

        ChefOrder order = getPendingOrders()
                .stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        if(order != null) {
            removeOrderFromPending(orderId);
            pushOrderToCancelled(order);
        }
    }

    public void moveOrderFromProcessingToCancel(Long orderId) {
        ChefOrder order = getProcessingOrders()
                .stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        if(order != null) {
            removeOrderFromProcessing(orderId);
            pushOrderToCancelled(order);
        }
    }

    public void moveOrderFromPausingToCancel(Long orderId) {
        ChefOrder order = getPausingOrders()
                .stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        if(order != null) {
            removeOrderFromPausing(orderId);
            pushOrderToCancelled(order);
        }
    }
}
