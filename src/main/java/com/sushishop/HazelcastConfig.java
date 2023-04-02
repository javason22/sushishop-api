package com.sushishop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.sushishop.cache.ChefSerializer;
import com.sushishop.cache.OrderStatusSerializer;
import com.sushishop.cache.SushiOrderSerializer;
import com.sushishop.entity.SushiOrder;
import com.sushishop.scheduler.Chef;
import com.sushishop.scheduler.OrderStatus;

@Configuration
public class HazelcastConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        

        // Set up your Hazelcast configuration here
        SerializationConfig serializationConfig = new SerializationConfig();
        // Add the custom serializer for Chef class
        serializationConfig.addSerializerConfig(new SerializerConfig().setTypeClass(Chef.class).setImplementation(new ChefSerializer()));

        // Add the custom serializer for SushiOrder class
        serializationConfig.addSerializerConfig(new SerializerConfig().setTypeClass(SushiOrder.class).setImplementation(new SushiOrderSerializer()));
        // Add the custom serializer for OrderStatus class
        serializationConfig.addSerializerConfig(new SerializerConfig().setTypeClass(OrderStatus.class).setImplementation(new OrderStatusSerializer()));

        Config config = new Config();
        config.setSerializationConfig(serializationConfig);
        return Hazelcast.newHazelcastInstance(config);
    }

}
