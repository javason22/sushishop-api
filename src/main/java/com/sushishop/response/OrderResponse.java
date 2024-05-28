package com.sushishop.response;

import com.sushishop.entity.SushiOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderResponse", description = "Order response")
public class OrderResponse extends BaseResponse{

    @Schema(name = "order", description = "Order")
    private SushiOrder order;

}
