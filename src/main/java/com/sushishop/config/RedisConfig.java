package com.sushishop.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@EnableCaching
@Configuration
public class RedisConfig {

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

    /*@EventListener
    public void onApplicationEvent(ApplicationReadyEvent event){
        cacheManager().getCacheNames()
                .parallelStream()
                .forEach(n -> cacheManager().getCache(n).clear());
    }

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // set cache TTL to 5 minutes
                .disableCachingNullValues();

        return RedisCacheManager.builder(lettuceConnectionFactory())
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }*/

    /*@Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // enable transaction support for redis so it participate Spring transaction management
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }*/
}
