package com.sushishop.controller;

import com.sushishop.request.OrderRequest;
import com.sushishop.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    

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
    public ResponseEntity<OrderResponse> add(@Valid @RequestBody OrderRequest request) {
        SushiOrder order;
        try {
            order = orderService.createOrder(sushi.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(BaseResponse.NORMAL_CODE, "Order created", order));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
        }
    }
}
