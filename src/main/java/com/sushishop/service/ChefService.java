package com.sushishop.service;

import com.sushishop.Constant;
import com.sushishop.entity.SushiOrder;
import com.sushishop.pojo.StatefulOrder;
import com.sushishop.repository.SushiOrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
                log.info("Chef {} is checking orders", index);
                if(!isMakingSushi.get(index).get()) { // if this chef is not making sushi
                    log.info("Chef {} is free", index);
                    // take order from pending queue, if any
                    StatefulOrder order = queueService.popOrderFromPending();
                    if(order == null) {
                        return;
                    }
                    order.setStartAt(Instant.now().toEpochMilli());
                    queueService.putOrderToProcessing(order); // put order to processing queue
                    sushiOrderRepository.updateOrderStatus(order.getOrderId(), statusService.findByName(Constant.STATUS_IN_PROGRESS));
                    isMakingSushi.get(index).set(true);
                    log.info("Chef {} is making sushi {}", index, order.getOrderId());
                } else {
                    log.info("Chef {} is busy", index);
                    // if this chef is making sushi, update progress
                    StatefulOrder order = queueService.getOrderFromProcessing(index);
                    long now = Instant.now().toEpochMilli();
                    order.setProgress(now - order.getStartAt()); // update progress
                    log.info("Order {} progress: {}/{}", order.getOrderId(), order.getProgress(), order.getTimeRequired());
                    // if order is completed, remove from processing queue
                    if(order.getProgress() >= order.getTimeRequired()) {
                        queueService.removeOrderFromProcessing(order.getOrderId());
                        sushiOrderRepository.updateOrderStatus(order.getOrderId(), statusService.findByName(Constant.STATUS_FINISHED));
                        isMakingSushi.get(index).set(false); // this chef is now free
                        log.info("Chef {} has completed order {}", index, order.getOrderId());
                    }
                }
                log.info("Chef {} is done checking orders", index);

            }, 0, 1000, TimeUnit.MILLISECONDS));
        }
    }
}
