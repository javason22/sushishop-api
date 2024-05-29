package com.sushishop.response;

import com.sushishop.entity.SushiOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Schema(name = "OrderResponse", description = "Order response")
public class OrderResponse extends BaseResponse{

    @Schema(name = "order", description = "Order")
    private Map<String, Object> order;

    public OrderResponse(int code, String message, Map<String, Object> order) {
        super(code, message);
        this.order = order;
    }

    public static OrderResponse from(int code, String message, SushiOrder sushiOrder) {
        Map<String, Object> order = Map.of(
                "id", sushiOrder.getId(),
                "statusId", sushiOrder.getStatus().getId(),
                "sushiId", sushiOrder.getSushi().getId(),
                "createdAt", sushiOrder.getCreatedAt().getTime());

        return new OrderResponse(code, message, order);
    }
}
