package com.sushishop.cache;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.sushishop.component.OrderTrackingItem;

import java.io.IOException;

public class OrderTrackingItemSerializer implements StreamSerializer<OrderTrackingItem> {
    
    @Override
    public int getTypeId() {
        // Assign a unique ID for your serializer
        return 3245;
    }

    @Override
    public void write(ObjectDataOutput out, OrderTrackingItem object) throws IOException {
        // Serialize your object to binary format
        out.writeLong(object.getOrderId());
        out.writeLong(object.getLastTrackTime());
        out.writeLong(object.getTimeSpent());
    }

    @Override
    public OrderTrackingItem read(ObjectDataInput in) throws IOException {
        // Deserialize your object from binary format
        long orderId = in.readLong();
        long lastTrackTime = in.readLong();
        long timeSpent = in.readLong();
        return new OrderTrackingItem(orderId, lastTrackTime, timeSpent);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
