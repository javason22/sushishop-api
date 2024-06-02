package com.sushishop;

import com.sushishop.service.ChefServiceExecutor;
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

    private final ChefServiceExecutor chefService;

    @Override
    public void run(ApplicationArguments args) {

        log.info("========= Sushi Shop started to take orders! =========");
        scheduler.schedule(chefService::run, 100, TimeUnit.MILLISECONDS);
        log.info("========= Sushi Shop finished taking orders! =========");
    }
}
