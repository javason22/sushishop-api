package com.sushishop.service;

import java.sql.Timestamp;
import java.util.List;

import com.sushishop.entity.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.sushishop.Constant;
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
        
        Status createdStatus = statusService.findByName(Constant.STATUS_CREATED);
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
        switch (order.getStatus().getName()) {
            case Constant.STATUS_CANCELLED:
                return false;
            case Constant.STATUS_FINISHED:
                return false;
            case Constant.STATUS_CREATED:
                queueService.removeOrderFromPending(orderId);
            case Constant.STATUS_IN_PROGRESS:
                queueService.removeOrderFromProcessing(orderId);
            case Constant.STATUS_PAUSED:
                queueService.removeOrderFromPausing(orderId);
        }
        if(statusService.findByName(Constant.STATUS_CANCELLED).equals(order.getStatus())){
            log.warn("Order ID {} is already cancelled", orderId);
            return false;
        }
        order.setStatus(statusService.findByName(Constant.STATUS_CANCELLED));
        orderRepository.save(order);
        return true;
    }

    public List<SushiOrder> listOrders(){
        return orderRepository.findAll();
    }

    @Transactional
    public boolean pauseOrder(long orderId){
        SushiOrder order = this.getOrder(orderId);
        if(!statusService.findByName(Constant.STATUS_IN_PROGRESS).equals(order.getStatus())){
            log.info("Cannot pause Order ID {} which is not in progress", orderId);
            return false;
        }
        if(!queueService.moveOrderFromProcessingToPausing(orderId)){
            log.error("Failed to move Order ID {} from processing to pausing", orderId);
            return false;
        }
        order.setStatus(statusService.findByName(Constant.STATUS_PAUSED));
        orderRepository.save(order);
        return true;
    }

    @Transactional
    public boolean resumeOrder(long orderId){
        SushiOrder order = this.getOrder(orderId);
        if(!statusService.findByName(Constant.STATUS_PAUSED).equals(order.getStatus())){
            log.info("Cannot resume Order ID {} which is not paused", orderId);
            return false;
        }
        if(!queueService.moveOrderFromPausingToPending(orderId)){
            log.error("Failed to move Order ID {} from pausing to processing", orderId);
            return false;
        }
        order.setStatus(statusService.findByName(Constant.STATUS_IN_PROGRESS));
        orderRepository.save(order);
        return true;
    }
}
