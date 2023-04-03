package com.sushishop.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.sushishop.Constant;
import com.sushishop.entity.SushiOrder;
import com.sushishop.repository.SushiOrderRepository;
import com.sushishop.service.CachedService;

import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class OrderScheduler {
    
    private static final Log logger = LogFactory.getLog(OrderScheduler.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private CachedService cachedService;

    @Autowired
    private SushiOrderRepository orderRepository;

    @PostConstruct
    public void init(){
        IScheduledExecutorService schedulerService = hazelcastInstance.getScheduledExecutorService("scheduler");
        schedulerService.scheduleAtFixedRate(() -> {
            
            logger.debug("######################## START ############################");
            // get the order queue from hazelcase cache
            IMap<String, LinkedList<SushiOrder>> map = hazelcastInstance.getMap("orderMap");
            // get order queue for process
            LinkedList<SushiOrder> queue = map.get("queue");
            // get order status map that used for displaying orders status API
            IMap<Long, OrderStatus> orderStatusMap = hazelcastInstance.getMap("orderStatusMap");

            if(queue == null){
                queue = new LinkedList<SushiOrder>();
            }

            // print order
            logger.debug("Print Queue start");

            queue.forEach((order) -> logger.debug("Order Queue Item: Order ID : " + order.getId() + " Status : " + order.getStatusId()));
            logger.debug("Print Queue end");


            // get all the three chef from hazelcast cache
            IList<Chef> cheives = hazelcastInstance.getList("cheives");
            if(cheives.size() == 0){
                for(int i = 0; i < Constant.MAX_CHEF; i++){
                    cheives.add(new Chef(i));
                }
            }
            // print chef
            cheives.forEach((chef) -> logger.debug(chef));

            // add new order to the queue
            List<SushiOrder> createdOrders = orderRepository.findByStatusId(
            cachedService.getStatusIdByName(Constant.STATUS_CREATED));
            logger.debug("Number of Order Created: " + createdOrders.size());
            for(SushiOrder order : createdOrders){
                if(!queue.contains(order)){
                    logger.debug("Created Order added to Queue : Order ID: " + order.getId());
                    queue.addLast(order);
                }
            }
            // remove cancelled order
            List<SushiOrder> cancelledOrders = orderRepository.findByStatusId(
            cachedService.getStatusIdByName(Constant.STATUS_CANCELLED));
            logger.debug("Number of Order Cancelled: " + cancelledOrders.size());
            for(SushiOrder order : cancelledOrders){
                if(queue.contains(order)){
                    logger.debug("Cancelled Order removed from Queue :  Order ID: " + order.getId());
                    queue.remove(order);
                    // update order status map
                    updateOrderStatus(order);
                }
            }
            // append order that was resumed from in-process with priority
            List<SushiOrder> inProgressOrders = orderRepository.findByStatusId(
            cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS));
            logger.debug("Number of Order In-Progress: " + inProgressOrders.size());
            outerLoop:
            for(SushiOrder order : inProgressOrders){
                //innerLoop:
                for(Chef chef : cheives){
                    if(chef.getOrderId().equals(order.getId())) continue outerLoop;
                }
                if(!queue.contains(order)){
                    logger.debug("In-progress Order added to Queue :  Order ID: " + order.getId());
                    queue.addFirst(order);
                }
            }
            // use the idled chef to process order
            for(int i = 0 ; i < cheives.size() ; i++){
                Chef chef = cheives.get(i);
                if(chef.isIdle() && !queue.isEmpty()){
                    SushiOrder order = queue.removeFirst();
                    logger.debug("Chef takes order :  Order ID: " + order.getId());
                    
                    if(Constant.STATUS_IN_PROGRESS.equals(cachedService.getStatusNameById(order.getStatusId()))){
                        // chef resume the order process
                        OrderStatus orderStatus = orderStatusMap.get(order.getId());
                        orderStatusMap.put(order.getId(), chef.resumeOrder(order, orderStatus, cachedService, orderRepository));
                    }else{
                        // chef takes new order
                        // put new order in order status map
                        orderStatusMap.put(order.getId(), chef.takeOrder(order, cachedService, orderRepository));
                    }
                    
                    logger.debug("Chef after taking order : " + chef);
                }else if(!chef.isIdle()){
                    logger.debug("Chef process order :  Order ID: " + chef.getOrderId());
                    // chef process order
                    OrderStatus orderStatus = chef.processOrder(cachedService, orderRepository);
                    // update order status map
                    orderStatusMap.put(orderStatus.getOrderId(), orderStatus);
                    logger.debug("Chef after processing order : " + chef);
                }
                // Update the element in the Hazelcast cache
                cheives.set(i, chef);
            }
            // update remaining orders in queue to order status map
            for(SushiOrder order: queue){
                updateOrderStatus(order);
            }
            // put the queue back to map
            map.put("queue", queue);
            //hazelcastInstance.getList("cheives").addAll(cheives);
            
            // print order
            logger.debug("Print Queue start");
            queue.forEach((order) -> logger.debug("Order Queue Item: Order ID : " + order.getId() + " Status : " + order.getStatusId()));
            logger.debug("Print Queue end");

            // print chef
            cheives.forEach((chef) -> logger.debug(chef));
            // print order status map
            orderStatusMap.forEach((key, value) -> logger.debug(key + " = " + value));

            logger.debug("######################## END ############################");

        }, 0, Constant.SCHEDULER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void updateOrderStatus(SushiOrder order){
        // get order status map that used for displaying orders status API
        IMap<Long, OrderStatus> orderStatusMap = hazelcastInstance.getMap("orderStatusMap");
        OrderStatus orderStatus = null;
        if(!orderStatusMap.containsKey(order.getId())){
            // add new order status to map
            orderStatus = new OrderStatus(order.getId(), 0L, 
                cachedService.getStatusNameById(order.getStatusId()), System.currentTimeMillis());
        }else{
            // update order status's status if it already exist
            orderStatus = orderStatusMap.get(order.getId());  
            orderStatus.setStatus(cachedService.getStatusNameById(order.getStatusId()));
        }
        orderStatusMap.put(orderStatus.getOrderId(), orderStatus);
    }
}
