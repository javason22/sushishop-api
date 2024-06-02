package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.entity.SushiOrder;
import com.sushishop.enums.StatusType;
import com.sushishop.pojo.ChefOrder;
import com.sushishop.repository.SushiOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class ChefServiceExecutor {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Constant.MAX_CHEF);

    private final List<ScheduledFuture<?>> makeSushiTasks = new ArrayList<>(Constant.MAX_CHEF);

    //private final List<AtomicBoolean> isMakingSushi = new ArrayList<>(Constant.MAX_CHEF);

    private final QueueService queueService;

    private final SushiOrderRepository sushiOrderRepository;

    private final RedissonClient redissonClient;

    private final StatusService statusService;

    private void init() {
        queueService.initProcessors(Constant.MAX_CHEF);
    }

    public void run() {

        init(); // initialize the chef service

        for(int i = 0; i < Constant.MAX_CHEF; i++) {
            final int index = i;
            final Chef chef = new Chef(i, false);
            makeSushiTasks.add(scheduler.scheduleAtFixedRate(() -> {
                RLock rLock = redissonClient.getLock("chefLock");
                try {
                    rLock.tryLock(1L, 10L, TimeUnit.SECONDS); // try to acquire the lock for 10 seconds

                    log.info("Chef {} is checking orders", index);
                    if (!chef.isWorking()) {
                        log.info("Chef {} is free", index);
                        // take order from pending queue, if any
                        ChefOrder order = queueService.popOrderFromPending();
                        if (order == null) { // no order to take
                            return;
                        }
                        log.info("Chef {} is taking order {}", index, order.getOrderId());
                        order.setLastUpdatedAt(Instant.now().toEpochMilli());
                        //chef.setOrder(order);
                        queueService.putOrderToProcessing(index, order); // put order to the processing queue
                        updateOrderStatus(order.getOrderId(), StatusType.IN_PROGRESS.getStatus());
                        chef.setWorking(true);
                        log.info("Chef {} start to make sushi {}", index, order.getOrderId());
                    } else {
                        log.info("Chef {} is busy", index);
                        // if this chef is making sushi, update progress
                        ChefOrder order = queueService.getOrderFromProcessing(index);
                        // if order is void (paused or cancelled), return
                        if(order.isVoid()){
                            chef.setWorking(false); // set chef to free
                            return;
                        }
                        chef.process(order);
                        //ChefOrder order = chef.getOrder();
                        log.info("Order {} progress: {}/{}", order.getOrderId(), order.getProgress(), order.getTimeRequired());
                        // if order is completed, remove from processing queue
                        if (order.finish()) {
                            updateOrderStatus(order.getOrderId(), StatusType.FINISHED.getStatus());
                            queueService.pushOrderToFinished(order); // push order to finished queue
                            queueService.putOrderToProcessing(index, ChefOrder.builder().build()); // reset order
                            //chef.setOrder(null);
                            chef.setWorking(false);
                            log.info("Chef {} has completed order {}", index, order.getOrderId());
                        } else {
                            queueService.putOrderToProcessing(index, order); // put order back to the processing queue
                        }
                    }
                    log.info("Chef {} is done checking orders", index);
                }catch (Exception e){
                    log.error("Error while checking orders", e);
                }finally {
                    rLock.unlock(); // release the lock
                }

            }, 0, Constant.SCHEDULER_INTERVAL, TimeUnit.MILLISECONDS));
        }
    }

    private void updateOrderStatus(Long orderId, String status) {

        SushiOrder order = sushiOrderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order not found"));
        order.setStatus(statusService.findByName(status));
        sushiOrderRepository.saveAndFlush(order);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class Chef{

        private int id;

        //private ChefOrder order = null;
        private boolean working;

        public void process(ChefOrder order){
            if(order != null){
                log.info("Chef {} is making sushi: {}", id, order.getOrderId());
                long now = Instant.now().toEpochMilli();
                order.setProgress(order.getProgress() + now - order.getLastUpdatedAt());
                order.setLastUpdatedAt(now);
                log.info("Chef {} order progress: {}/{}", id, order.getProgress(), order.getTimeRequired());
            }
        }
    }
}
