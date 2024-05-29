package com.sushishop.controller;

import com.sushishop.entity.SushiOrder;
import com.sushishop.request.OrderRequest;
import com.sushishop.response.BaseResponse;
import com.sushishop.response.OrderResponse;
import com.sushishop.response.OrderStatusResponse;
import com.sushishop.service.OrderService;
import com.sushishop.service.StatusService;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final SushiService sushiService;

    private final OrderService orderService;

    private final StatusService statusService;

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
        if(!orderService.cancelOrder(id)){
            return ResponseEntity.badRequest().body(
                    new BaseResponse(BaseResponse.ERROR_CODE, "Failed to cancel order"));
        }
        // return order response
        return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order cancelled"));
    }

    @Operation(summary = "Get orders group by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, List<OrderStatusResponse>>> getOrders(){
        // get orders
        List<SushiOrder> orders = orderService.listOrders();
        // grouping
        Map<String, List<OrderStatusResponse>> orderMap = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getStatus().getName(), // group by status name
                        Collectors.mapping(order -> OrderStatusResponse.builder() // map to order status response
                                .orderId(order.getId())
                                .timeSpent((Instant.now().toEpochMilli() - order.getCreatedAt().getTime() / 1000)) // calculate time spent
                                        .build(), Collectors.toList())));
        // return order response
        return ResponseEntity.ok(orderMap);
    }

    @Operation(summary = "Pause an order")
    @Parameters({
            @Parameter(name = "id", description = "Order ID", required = true, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order paused"),
            @ApiResponse(responseCode = "400", description = "Bad request. Failed to pause order"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/pause")
    public ResponseEntity<BaseResponse> pauseOrder(@PathVariable("id") Long id){
        // pause order
        if(!orderService.pauseOrder(id)){
            return ResponseEntity.badRequest().body(
                    new BaseResponse(BaseResponse.ERROR_CODE, "Failed to pause order"));
        }
        // return order response
        return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order paused"));
    }

    @Operation(summary = "Resume an order")
    @Parameters({
            @Parameter(name = "id", description = "Order ID", required = true, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order resumed"),
            @ApiResponse(responseCode = "400", description = "Bad request. Failed to resume order"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/resume")
    public ResponseEntity<BaseResponse> resumeOrder(@PathVariable("id") Long id){
        // resume order
        if(!orderService.resumeOrder(id)){
            return ResponseEntity.badRequest().body(
                    new BaseResponse(BaseResponse.ERROR_CODE, "Failed to resume order"));
        }
        // return order response
        return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order resumed"));
    }

}
