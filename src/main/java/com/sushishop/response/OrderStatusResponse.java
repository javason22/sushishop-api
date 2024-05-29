package com.sushishop.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(name = "OrderStatusResponse", description = "Order status response")
public class OrderStatusResponse {

    @Schema(name = "orderId", description = "Order ID")
    private Long orderId;

    @Schema(name = "statusId", description = "Time spent for order processing")
    private Long timeSpent;
}
