package com.sushishop.service;

import com.sushishop.entity.SushiOrder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@AllArgsConstructor
public class ChefService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private ScheduledFuture<?> makeSushiTask;

    private final AtomicBoolean isMakingSushi = new AtomicBoolean(false);

    private final QueueService queueService;

    private final OrderService orderService;

    public void takeOrder() {

        Long orderId = queueService.getOrderFromQueue();
        if (orderId == null) {
            return;
        }
        SushiOrder order = orderService.getOrder(orderId);
        assert order != null;

        if (isMakingSushi.get()) {
            return;
        }
        isMakingSushi.set(true);
        makeSushiTask = scheduler.schedule(() -> {
            isMakingSushi.set(false);
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void pauseOrder() {
        if (isMakingSushi.get()) {
            makeSushiTask.cancel(true);
            isMakingSushi.set(false);
        }
    }
}
