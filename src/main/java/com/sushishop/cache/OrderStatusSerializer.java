package com.sushishop.cache;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.sushishop.scheduler.OrderStatus;

public class OrderStatusSerializer implements StreamSerializer<OrderStatus> {
    
    @Override
    public int getTypeId() {
        // Assign a unique ID for your serializer
        return 4444;
    }

    @Override
    public void write(ObjectDataOutput out, OrderStatus object) throws IOException {
        // Serialize your object to binary format
        out.writeLong(object.getOrderId());
        out.writeLong(object.getTimeSpent());
        out.writeString(object.getStatus());
        out.writeLong(object.getLastUpdateTime());
    }

    @Override
    public OrderStatus read(ObjectDataInput in) throws IOException {
        // Deserialize your object from binary format
        Long orderId = in.readLong();
        Long timeSpent = in.readLong();
        String status = in.readString();
        Long lastUpdateTime = in.readLong();
        return new OrderStatus(orderId, timeSpent, status, lastUpdateTime);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
    
}
