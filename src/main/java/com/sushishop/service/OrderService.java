package com.sushishop.service;

import java.sql.Timestamp;
import java.util.Collection;

import com.sushishop.entity.Status;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.sushishop.Constant;
import com.sushishop.entity.Sushi;
import com.sushishop.entity.SushiOrder;
import com.sushishop.exception.OrderAlreadyCancelledException;
import com.sushishop.exception.OrderAlreadyFinishedException;
import com.sushishop.exception.OrderNotFoundException;
import com.sushishop.exception.OrderNotPausedException;
import com.sushishop.repository.SushiOrderRepository;
import com.sushishop.scheduler.OrderStatus;

import jakarta.transaction.Transactional;

@Service
@AllArgsConstructor
public class OrderService {

    private final SushiOrderRepository orderRepository;

    private final CachedService cachedService;

    private final HazelcastInstance hazelcastInstance;

    private final StatusService statusService;
    

    public SushiOrder createOrder(String sushiName) {
        Sushi sushi = cachedService.getSushiByName(sushiName);
        
        Status createdStatus = statusService.findByName(Constant.STATUS_CREATED);
        assert createdStatus != null; // created status should be available
        SushiOrder order = SushiOrder.builder()
                .sushiId(sushi.getId())
                .statusId(createdStatus.getId())
                .createdAt(new Timestamp(System.currentTimeMillis())).build();

        return orderRepository.save(order);
    }

    public SushiOrder cancelOrder(Long orderId) 
    throws OrderAlreadyFinishedException, OrderAlreadyCancelledException, OrderNotFoundException{
        // find the order
        //SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        //    new OrderNotFoundException("Order not found with ID: " + orderId));

        // modified to be Java8 compatible
        SushiOrder order = orderRepository.findById(orderId).orElseThrow(() -> 
            new OrderNotFoundException("Order not found with ID: " + orderId));
        // check if the order was finished
        if (order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_FINISHED))) {
            throw new OrderAlreadyFinishedException("Cannot cancel order because the order has already been finished.");
        }
        // check if the order was cancelled
        if (order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_CANCELLED))) {
            throw new OrderAlreadyCancelledException("Cannot cancel order because the order has already been cancelled.");
        }
        // update order status
        Integer cancelStatusId = cachedService.getStatusIdByName(Constant.STATUS_CANCELLED);
        order.setStatusId(cancelStatusId);
        return orderRepository.save(order);

    }

    public Collection<OrderStatus> listOrders(){
        //return orderRepository.findAll();
        IMap<Long, OrderStatus> orderStatusMap = hazelcastInstance.getMap("orderStatusMap");
        return orderStatusMap.values();
    }

    public SushiOrder pauseOrder(long orderId) throws OrderNotFoundException, OrderAlreadyFinishedException, OrderAlreadyCancelledException{
        // find the order
        //SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        //new OrderNotFoundException("Order not found with ID: " + orderId));
        
        // modified to be Java8 compatible
        SushiOrder order = orderRepository.findById(orderId).orElseThrow(() -> 
            new OrderNotFoundException("Order not found with ID: " + orderId));
        
        String status = cachedService.getStatusNameById(order.getStatusId());
        // check if the order was finished
        if (Constant.STATUS_FINISHED.equals(status)) {
            throw new OrderAlreadyFinishedException("Cannot pause the order because the order has already been finished.");
        }
        // check if the order was cancelled
        if (Constant.STATUS_CANCELLED.equals(status)) {
            throw new OrderAlreadyCancelledException("Cannot pause the order because the order has already been cancelled.");
        }

        // update order status
        if (!Constant.STATUS_PAUSED.equals(status)) {
            Integer pauseStatusId = cachedService.getStatusIdByName(Constant.STATUS_PAUSED);
            order.setStatusId(pauseStatusId);
            return orderRepository.save(order);
        }
        return order;
    }

    public SushiOrder resumeOrder(long orderId) throws OrderNotFoundException, OrderNotPausedException{
        // find the order
        //SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        //new OrderNotFoundException("Order not found with ID: " + orderId));

        // modified to be Java8 compatible
        SushiOrder order = orderRepository.findById(orderId).orElseThrow(() -> 
            new OrderNotFoundException("Order not found with ID: " + orderId));

        // check if the order was in paused. Only paused order can be resumed.
        if (!order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_PAUSED))) {
            throw new OrderNotPausedException("Cannot resume the order because the order was not paused.");
        }
        
        IMap<Long, OrderStatus> orderStatusMap = hazelcastInstance.getMap("orderStatusMap");
        // status resume to create if order has not been started, or in-progress already started before
        String status = (orderStatusMap.get(order.getId()).getTimeSpent() == 0) ? Constant.STATUS_CREATED : Constant.STATUS_IN_PROGRESS;

        // notify the tracking component to resume by updating the timer
        // update order status
        order.setStatusId(cachedService.getStatusIdByName(status));
        return orderRepository.save(order);
    }
}
