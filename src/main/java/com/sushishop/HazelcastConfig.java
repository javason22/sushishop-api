package com.sushishop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.sushishop.cache.OrderTrackingItemSerializer;
import com.sushishop.component.OrderTrackingItem;

@Configuration
public class HazelcastConfig {
    @Bean
    public HazelcastInstance hazelcastInstance() {
        SerializationConfig serializationConfig = new SerializationConfig()
            .addSerializerConfig(new SerializerConfig()
                .setTypeClass(OrderTrackingItem.class)
                .setImplementation(new OrderTrackingItemSerializer()));
        // Set up your Hazelcast configuration here
        return Hazelcast.newHazelcastInstance(new Config().setSerializationConfig(serializationConfig));
    }
}
