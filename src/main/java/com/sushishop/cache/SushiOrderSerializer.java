package com.sushishop.cache;

import java.io.IOException;
import java.sql.Timestamp;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.sushishop.entity.SushiOrder;

public class SushiOrderSerializer implements StreamSerializer<SushiOrder> {
    
    @Override
    public int getTypeId() {
        // Assign a unique ID for your serializer
        return 2222;
    }

    @Override
    public void write(ObjectDataOutput out, SushiOrder object) throws IOException {
        // Serialize your object to binary format
        out.writeLong(object.getCreatedAt().getTime());
        out.writeLong(object.getId());
        out.writeInt(object.getStatusId());
        out.writeLong(object.getSushiId());
    }

    @Override
    public SushiOrder read(ObjectDataInput in) throws IOException {
        // Deserialize your object from binary format
        Timestamp createdAt = new Timestamp(in.readLong());
        Long id = in.readLong();
        Integer statusId = in.readInt();
        Long sushiId = in.readLong();
        return new SushiOrder(id, statusId, sushiId, createdAt);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
    
}
