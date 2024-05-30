package com.sushishop.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

@Slf4j
@EnableCaching
@Configuration
public class RedisConfig {

    private RedisServer redisServer;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @PostConstruct
    public void redisServer() {
        try{
            redisServer = new RedisServer(redisPort);
            redisServer.start();
        } catch (Exception e) {
            log.error("Error starting redis server", e);
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if(redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory());
        // enable transaction support for redis so it participate Spring transaction management
        //redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

}
