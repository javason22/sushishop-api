package com.sushishop.cache;

import java.io.IOException;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.sushishop.scheduler.Chef;

public class ChefSerializer implements StreamSerializer<Chef> {
    
    @Override
    public int getTypeId() {
        // Assign a unique ID for your serializer
        return 1111;
    }

    @Override
    public void write(ObjectDataOutput out, Chef object) throws IOException {
        // Serialize your object to binary format
        out.writeInt(object.getChefId());
        out.writeBoolean(object.isIdle());
        out.writeLong(object.getLastUpdateTime());
        out.writeLong(object.getTimeSpent());
        out.writeLong(object.getTimeToMake());
        out.writeLong(object.getOrderId());
    }

    @Override
    public Chef read(ObjectDataInput in) throws IOException {
        // Deserialize your object from binary format
        Integer chefId = in.readInt();
        boolean idle = in.readBoolean();
        Long lastUpdateTime = in.readLong();
        Long timeSpent = in.readLong();
        Long timeToMake = in.readLong();
        Long orderId = in.readLong();
        return new Chef(chefId, idle, lastUpdateTime, timeToMake, timeSpent, orderId);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
    
}
