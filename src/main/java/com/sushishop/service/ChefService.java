package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.entity.SushiOrder;
import com.sushishop.pojo.StatefulOrder;
import com.sushishop.repository.SushiOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@AllArgsConstructor
public class ChefService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Constant.MAX_CHEF);

    private final List<ScheduledFuture<?>> makeSushiTasks = new ArrayList<>(Constant.MAX_CHEF);

    private final List<AtomicBoolean> isMakingSushi = new ArrayList<>(Constant.MAX_CHEF);

    private final QueueService queueService;

    private final SushiOrderRepository sushiOrderRepository;

    private final RedissonClient redissonClient;

    private final StatusService statusService;

    private void init() {
        for(int i = 0; i < Constant.MAX_CHEF; i++) {
            isMakingSushi.add(new AtomicBoolean(false));
        }
    }

    public void run() {

        init(); // initialize the chef service

        for(int i = 0; i < Constant.MAX_CHEF; i++) {
            final int index = i;
            makeSushiTasks.add(scheduler.scheduleAtFixedRate(() -> {
                RLock rLock = redissonClient.getLock("chefLock");
                try {
                    rLock.tryLock(1L, 10L, TimeUnit.SECONDS);

                    log.info("Chef {} is checking orders", index);
                    if (!isMakingSushi.get(index).get()) { // if this chef is not making sushi
                        log.info("Chef {} is free", index);
                        // take order from pending queue, if any
                        StatefulOrder order = queueService.popOrderFromPending();
                        if (order == null) {
                            return;
                        }
                        log.info("Chef {} is taking order {}", index, order.getOrderId());
                        order.setStartAt(Instant.now().toEpochMilli());
                        queueService.putOrderToProcessing(order); // put order to the processing queue
                        updateOrderStatus(order.getOrderId(), Constant.STATUS_IN_PROGRESS);
                        isMakingSushi.get(index).set(true);
                        log.info("Chef {} is making sushi {}", index, order.getOrderId());
                    } else {
                        log.info("Chef {} is busy", index);
                        // if this chef is making sushi, update progress
                        StatefulOrder order = queueService.getOrderFromProcessing();
                        long now = Instant.now().toEpochMilli();
                        order.setProgress(now - order.getStartAt()); // update progress
                        log.info("Order {} progress: {}/{}", order.getOrderId(), order.getProgress(), order.getTimeRequired());
                        // if order is completed, remove from processing queue
                        if (order.getProgress() >= order.getTimeRequired()) {
                            //queueService.removeOrderFromProcessing(order.getOrderId());
                            updateOrderStatus(order.getOrderId(), Constant.STATUS_FINISHED);
                            isMakingSushi.get(index).set(false); // this chef is now free
                            log.info("Chef {} has completed order {}", index, order.getOrderId());
                        } else {
                            queueService.putOrderToProcessing(order); // put order back to the processing queue
                        }
                    }
                    log.info("Chef {} is done checking orders", index);
                }catch (Exception e){
                    log.error("Error while checking orders", e);
                }finally {
                    rLock.unlock(); // release the lock
                }

            }, 0, 1000, TimeUnit.MILLISECONDS));
        }
    }

    private void updateOrderStatus(Long orderId, String status) {

        SushiOrder order = sushiOrderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order not found"));
        order.setStatus(statusService.findByName(status));
        sushiOrderRepository.saveAndFlush(order);
    }
}
