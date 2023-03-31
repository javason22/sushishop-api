package com.sushishop.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sushishop.Constant;
import com.sushishop.component.OrderTracking;
import com.sushishop.entity.Status;
import com.sushishop.entity.Sushi;
import com.sushishop.entity.SushiOrder;
import com.sushishop.exception.OrderAlreadyCancelledException;
import com.sushishop.exception.OrderAlreadyFinishedException;
import com.sushishop.exception.OrderNotFoundException;
import com.sushishop.exception.OrderNotPausedException;
import com.sushishop.exception.SushiNotFoundException;
import com.sushishop.repository.StatusRepository;
import com.sushishop.repository.SushiOrderRepository;
import com.sushishop.repository.SushiRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    private Map<String, Integer> statuses = new HashMap<>();

    @Autowired
    private SushiRepository sushiRepository;

    @Autowired
    private SushiOrderRepository orderRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private OrderTracking orderTracking;

    @PostConstruct
    public void init() {
        // initialize status so it only read from DB once
        List<Status> statusList = statusRepository.findAll();
        for(Status status : statusList){
            statuses.put(status.getName(), status.getId());
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void processOrders() {
        List<SushiOrder> createdOrders = orderRepository.findByStatusId(
            statuses.get(Constant.STATUS_CREATED));
        List<SushiOrder> inProgressOrders = orderRepository.findByStatusId(
            statuses.get(Constant.STATUS_IN_PROGRESS));

        // Check if there are less than 3 orders in progress and has new order pending
        while (inProgressOrders.size() < 3 && !createdOrders.isEmpty()){
            // remove the first order from list and add to in progress list
            SushiOrder order = createdOrders.remove(0);
            order.setStatusId(statuses.get(Constant.STATUS_IN_PROGRESS));
            inProgressOrders.add(order);

            // Update order status to "in-progress"
            orderRepository.save(order);
            // track the start time to make the sushi
            orderTracking.trackOrderTime(order.getId());
        }

        // Check in-progress orders
        for (SushiOrder order : inProgressOrders) {
            Sushi sushi = sushiRepository.findById(order.getSushiId()).orElse(null);

            // Check if the order is ready
            orderTracking.trackOrderTime(order.getId());
            long timeSpent = orderTracking.getTimeSpent(order.getId());
            if (timeSpent >= sushi.getTimeToMake()) {
                // Update order status to "finished"
                order.setStatusId(statuses.get(Constant.STATUS_FINISHED));
                orderRepository.save(order);
            }
        }
    }

    public SushiOrder createOrder(String sushiName) throws SushiNotFoundException {
        Sushi sushi = sushiRepository.findByName(sushiName).stream().findFirst()
                .orElseThrow(() -> new SushiNotFoundException("Sushi not found with name: " + sushiName));
        
        Integer createdStatusId = statuses.get(Constant.STATUS_CREATED);
        SushiOrder order = new SushiOrder();
        order.setSushiId(sushi.getId());
        order.setStatusId(createdStatusId);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        SushiOrder resultOrder = orderRepository.save(order);

        orderTracking.trackOrderTime(resultOrder.getId());

        return orderRepository.save(order);
    }

    public SushiOrder cancelOrder(Long orderId) 
    throws OrderAlreadyFinishedException, OrderAlreadyCancelledException, OrderNotFoundException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
            new OrderNotFoundException("Order not found with ID: " + orderId));
        // check if the order was finished
        if (order.getStatusId().equals(statuses.get(Constant.STATUS_FINISHED))) {
            throw new OrderAlreadyFinishedException("Cannot cancel order because the order has already been finished.");
        }
        // check if the order was cancelled
        if (order.getStatusId().equals(statuses.get(Constant.STATUS_CANCELLED))) {
            throw new OrderAlreadyCancelledException("Cannot cancel order because the order has already been cancelled.");
        }
        // update order status
        Integer cancelStatusId = statuses.get(Constant.STATUS_CANCELLED);
        order.setStatusId(cancelStatusId);
        return orderRepository.save(order);

    }

    /*public Map<String, List<SushiOrder>> listOrders(){
        // query all statuses
        List<Status> allStatuses = statusRepository.findAll();
        // retrieve all orders by statuses
        Map<String, List<SushiOrder>> results = new HashMap<String, List<SushiOrder>>();
        for(Status status : allStatuses){
            results.put(status.getName(), orderRepository.findByStatusId(status.getId()));
        }

        return results;
    }*/
    public List<SushiOrder> listOrders(){
        return orderRepository.findAll();
    }

    public Map<String, Integer> getStatusMap(){
        return statuses;
    }

    public SushiOrder pauseOrder(long orderId) throws OrderNotFoundException, OrderAlreadyFinishedException, OrderAlreadyCancelledException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        new OrderNotFoundException("Order not found with ID: " + orderId));
        
        // check if the order was finished
        if (order.getStatusId().equals(statuses.get(Constant.STATUS_FINISHED))) {
            throw new OrderAlreadyFinishedException("Cannot pause the order because the order has already been finished.");
        }
        // check if the order was cancelled
        if (order.getStatusId().equals(statuses.get(Constant.STATUS_CANCELLED))) {
            throw new OrderAlreadyCancelledException("Cannot pause the order because the order has already been cancelled.");
        }

        // update order status
        Integer pauseStatusId = statuses.get(Constant.STATUS_PAUSED);
        order.setStatusId(pauseStatusId);
        return orderRepository.save(order);
    }

    public SushiOrder resumeOrder(long orderId) throws OrderNotFoundException, OrderNotPausedException{
        // find the order
        SushiOrder order = orderRepository.findById(orderId).stream().findFirst().orElseThrow(() -> 
        new OrderNotFoundException("Order not found with ID: " + orderId));

        // check if the order was in paused. Only paused order can be resumed.
        if (!order.getStatusId().equals(statuses.get(Constant.STATUS_PAUSED))) {
            throw new OrderNotPausedException("Cannot resume the order because the order was not paused.");
        }
        
        // notify the tracking component to resume by updating the timer
        orderTracking.resumeTracking(orderId);
        // update order status
        Integer inProgressStatusId = statuses.get(Constant.STATUS_IN_PROGRESS);
        order.setStatusId(inProgressStatusId);
        return orderRepository.save(order);
    }
}
