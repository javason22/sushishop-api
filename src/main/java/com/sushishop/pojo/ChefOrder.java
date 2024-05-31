package com.sushishop.pojo;

import com.sushishop.entity.Status;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class ChefOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId; // order id

    private Long startAt; // time when order was taken by chef

    private Long timeRequired; // time required to finish the sushi

    private Long progress; // progress of sushi making

    @Transient
    private Status status; // status of the order - temporary

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChefOrder that = (ChefOrder) obj;
        return orderId.equals(that.orderId);
    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }

    public boolean finish() {
        return progress >= timeRequired;
    }

    public boolean isVoid(){
        return orderId == null;
    }
}
