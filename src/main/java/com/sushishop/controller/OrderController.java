package com.sushishop.controller;

import com.sushishop.entity.SushiOrder;
import com.sushishop.request.OrderRequest;
import com.sushishop.response.BaseResponse;
import com.sushishop.response.OrderResponse;
import com.sushishop.service.OrderService;
import com.sushishop.service.SushiService;
import com.sushishop.entity.Sushi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final SushiService sushiService;

    private final OrderService orderService;

    @Operation(summary = "Create order")
    @Parameters({
            @Parameter(name = "sushi_name", description = "Sushi name", required = true, example = "California Roll")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Validation exception"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("")
    public ResponseEntity<BaseResponse> order(@Valid @RequestBody OrderRequest request) {
        Sushi sushi = sushiService.getSushiByName(request.getSushiName());
        if (sushi == null) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, "Sushi not found"));
        }
        // create order
        SushiOrder order = orderService.createOrder(request.getSushiName());
        // return order response
        return ResponseEntity.ok(OrderResponse.from(BaseResponse.NORMAL_CODE, "Order created", order));
    }

    @Operation(summary = "Cancel order")
    @Parameters({
            @Parameter(name = "id", description = "Order ID", required = true, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> cancelOrder(@PathVariable("id") Long id){
        // cancel order
        SushiOrder order = orderService.cancelOrder(id);
        // return order response
        return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order cancelled"));
    }
}
