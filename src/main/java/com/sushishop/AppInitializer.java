package com.sushishop;

import com.sushishop.service.ChefService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Component
public class AppInitializer implements ApplicationRunner {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private final ChefService chefService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========= Sushi Shop started to take orders! =========");
        scheduler.schedule(() -> {
            chefService.takeOrder();
        }, 100, TimeUnit.MILLISECONDS);
        log.info("========= Sushi Shop finished taking orders! =========");
    }
}
