package com.sushishop.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.sushishop.Constant;
import com.sushishop.component.OrderTracking;
import com.sushishop.entity.Sushi;
import com.sushishop.entity.SushiOrder;
import com.sushishop.exception.OrderAlreadyCancelledException;
import com.sushishop.exception.OrderAlreadyFinishedException;
import com.sushishop.exception.OrderNotFoundException;
import com.sushishop.exception.OrderNotPausedException;
import com.sushishop.repository.SushiOrderRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    //@Autowired
    //private SushiRepository sushiRepository;

    @Autowired
    private SushiOrderRepository orderRepository;

    @Autowired
    private OrderTracking orderTracking;

    @Autowired
    private CachedService cachedService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @PostConstruct
    public void init(){
        IScheduledExecutorService schedulerService = hazelcastInstance.getScheduledExecutorService("scheduler");
        schedulerService.scheduleAtFixedRate(() -> {
            List<SushiOrder> createdOrders = orderRepository.findByStatusId(
            cachedService.getStatusIdByName(Constant.STATUS_CREATED));
            List<SushiOrder> inProgressOrders = orderRepository.findByStatusId(
                cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS));

            // Check if there are less than 3 orders in progress and has new order pending
            while (inProgressOrders.size() < Constant.MAX_CHEF && !createdOrders.isEmpty()){
                // remove the first order from list and add to in progress list
                SushiOrder order = createdOrders.remove(0);
                order.setStatusId(cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS));
                inProgressOrders.add(order);

                // Update order status to "in-progress"
                orderRepository.save(order);
                // track the start time to make the sushi
                orderTracking.trackOrderTime(order.getId());
            }

            // Check in-progress orders
            for (SushiOrder order : inProgressOrders) {
                Sushi sushi = cachedService.getSushiById(order.getSushiId());

                // Check if the order is ready
                orderTracking.updateOrderTime(order.getId());
                long timeSpent = orderTracking.getTimeSpent(order.getId());
                if (timeSpent >= sushi.getTimeToMake()) {
                    // Update order status to "finished"
                    order.setStatusId(cachedService.getStatusIdByName(Constant.STATUS_FINISHED));
                    orderRepository.save(order);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public SushiOrder createOrder(String sushiName) {
        Sushi sushi = cachedService.getSushiByName(sushiName);
        
        Integer createdStatusId = cachedService.getStatusIdByName(Constant.STATUS_CREATED);
        SushiOrder order = new SushiOrder();
        order.setSushiId(sushi.getId());
        order.setStatusId(createdStatusId);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return orderRepository.save(order);
    }

    public SushiOrder cancelOrder(Long orderId) 
    throws OrderAlreadyFinishedException, OrderAlreadyCancelledException, OrderNotFoundException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
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

    public List<SushiOrder> listOrders(){
        return orderRepository.findAll();
    }

    public SushiOrder pauseOrder(long orderId) throws OrderNotFoundException, OrderAlreadyFinishedException, OrderAlreadyCancelledException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        new OrderNotFoundException("Order not found with ID: " + orderId));
        
        // check if the order was finished
        if (order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_FINISHED))) {
            throw new OrderAlreadyFinishedException("Cannot pause the order because the order has already been finished.");
        }
        // check if the order was cancelled
        if (order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_CANCELLED))) {
            throw new OrderAlreadyCancelledException("Cannot pause the order because the order has already been cancelled.");
        }

        // update order status
        Integer pauseStatusId = cachedService.getStatusIdByName(Constant.STATUS_PAUSED);
        order.setStatusId(pauseStatusId);
        return orderRepository.save(order);
    }

    public SushiOrder resumeOrder(long orderId) throws OrderNotFoundException, OrderNotPausedException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        new OrderNotFoundException("Order not found with ID: " + orderId));

        // check if the order was in paused. Only paused order can be resumed.
        if (!order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_PAUSED))) {
            throw new OrderNotPausedException("Cannot resume the order because the order was not paused.");
        }
        
        // notify the tracking component to resume by updating the timer
        orderTracking.resumeTracking(orderId);
        // update order status
        Integer inProgressStatusId = cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS);
        order.setStatusId(inProgressStatusId);
        return orderRepository.save(order);
    }
}
