package com.sushishop.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sushishop.entity.Status;
import com.sushishop.enums.StatusType;
import com.sushishop.pojo.ChefOrder;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.sushishop.entity.Sushi;
import com.sushishop.entity.SushiOrder;
import com.sushishop.repository.SushiOrderRepository;

import jakarta.transaction.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {

    private final SushiService sushiService;

    private final StatusService statusService;
    
    private final SushiOrderRepository orderRepository;

    private final QueueService queueService;

    @Cacheable(value = "order", key = "#id", unless = "#result == null")
    public SushiOrder getOrder(Long id){
        return orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order not found"));
    }

    @Transactional
    public SushiOrder createOrder(String sushiName) {
        Sushi sushi = sushiService.getSushiByName(sushiName);
        
        Status createdStatus = statusService.findByName(StatusType.CREATED.getStatus());
        assert createdStatus != null; // created status should be available
        SushiOrder order = SushiOrder.builder()
                .sushi(sushi)
                .status(createdStatus)
                .createdAt(new Timestamp(System.currentTimeMillis())).build();
        order =  orderRepository.save(order); // save order to db and get id
        // push order to the pending queue
        queueService.pushOrderToPending(order.getId(), sushi.getTimeToMake());
        return order;
    }

    @Transactional
    public boolean cancelOrder(Long orderId){
        SushiOrder order = orderRepository.findById(orderId).orElse(null);
        if(order == null){
            throw new EntityNotFoundException("Order not found");
        }
        // check if order is already cancelled
        StatusType statusType = StatusType.getFromString(order.getStatus().getName());
        switch (Objects.requireNonNull(statusType)) {
            case CANCELLED, FINISHED:
                return false;
            case CREATED:
                queueService.moveOrderFromPendingToCancel(orderId);
            case IN_PROGRESS:
                queueService.moveOrderFromProcessingToCancel(orderId);
            case PAUSED:
                queueService.moveOrderFromPausingToCancel(orderId);
        }
        if(StatusType.CANCELLED == StatusType.getFromString(order.getStatus().getName())){
            log.warn("Order ID {} is already cancelled", orderId);
            return false;
        }

        order.setStatus(statusService.findByName(StatusType.CANCELLED.getStatus()));
        orderRepository.save(order);
        return true;
    }

    public List<ChefOrder> listChefOrders(){
        // get orders
        List<ChefOrder> pendingOrders = queueService.getPendingOrders();
        List<ChefOrder> processingOrders = queueService.getProcessingOrders().stream().filter(o -> !o.isVoid()).toList();
        List<ChefOrder> pausedOrders = queueService.getPausingOrders();
        List<ChefOrder> finishedOrders = queueService.getFinishedOrders();
        List<ChefOrder> cancelledOrders = queueService.getCancelledOrders();

        pendingOrders.forEach(o -> o.setStatus(statusService.findByName(StatusType.CREATED.getStatus())));
        processingOrders.forEach(o -> o.setStatus(statusService.findByName(StatusType.IN_PROGRESS.getStatus())));
        pausedOrders.forEach(o -> o.setStatus(statusService.findByName(StatusType.PAUSED.getStatus())));
        finishedOrders.forEach(o -> o.setStatus(statusService.findByName(StatusType.FINISHED.getStatus())));
        cancelledOrders.forEach(o -> o.setStatus(statusService.findByName(StatusType.CANCELLED.getStatus())));

        return Stream.of(pendingOrders, processingOrders, pausedOrders, finishedOrders, cancelledOrders)
                .flatMap(List::stream).collect(Collectors.toList());
    }

    @Transactional
    public boolean pauseOrder(long orderId){
        SushiOrder order = getOrder(orderId);
        if(StatusType.IN_PROGRESS != StatusType.getFromString(order.getStatus().getName())){
            log.info("Cannot pause Order ID {} which is not in progress", orderId);
            return false;
        }
        if(!queueService.moveOrderFromProcessingToPausing(orderId)){
            log.error("Failed to move Order ID {} from processing to pausing", orderId);
            return false;
        }
        order.setStatus(statusService.findByName(StatusType.PAUSED.getStatus()));
        orderRepository.save(order);
        return true;
    }

    @Transactional
    public boolean resumeOrder(long orderId){
        SushiOrder order = getOrder(orderId);
        if(StatusType.PAUSED != StatusType.getFromString(order.getStatus().getName())){
            log.info("Cannot resume Order ID {} which is not paused", orderId);
            return false;
        }
        if(!queueService.moveOrderFromPausingToPending(orderId)){
            log.error("Failed to move Order ID {} from pausing to processing", orderId);
            return false;
        }
        // do not update status, keep it paused until it is picked up by a chef
        return true;
    }
}
