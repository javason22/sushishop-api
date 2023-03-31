package com.sushishop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sushishop.component.OrderTracking;
import com.sushishop.entity.Sushi;
import com.sushishop.entity.SushiOrder;
import com.sushishop.exception.OrderAlreadyCancelledException;
import com.sushishop.exception.OrderAlreadyFinishedException;
import com.sushishop.exception.OrderNotFoundException;
import com.sushishop.exception.OrderNotPausedException;
import com.sushishop.exception.SushiNotFoundException;
import com.sushishop.response.BaseResponse;
import com.sushishop.response.OrderResponse;
import com.sushishop.response.StatusResponse;
import com.sushishop.service.OrderService;

@RestController
@RequestMapping("api")
public class SushiController {
    
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderTracking orderTracking;

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createOrder(@RequestBody Sushi sushi){
        SushiOrder order;
        try {
            order = orderService.createOrder(sushi.getName());
            //return ResponseEntity.ok(new OrderResponse(BaseResponse.NORMAL_CODE, "Order created", order));
            return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(BaseResponse.NORMAL_CODE, "Order created", order));
        } catch (SushiNotFoundException e) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
        }
        
    }

    @DeleteMapping("/orders/{order_id}")
    public ResponseEntity<?> cancelOrder(@PathVariable("order_id") Long orderId){

        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order cancelled"));
        } catch (OrderAlreadyFinishedException | OrderAlreadyCancelledException | OrderNotFoundException e) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
        } 

    }

    @GetMapping("/orders/status")
    public ResponseEntity<?> listOrders(){
        List<SushiOrder> orders = orderService.listOrders();
        Map<String, Integer> statusMap = orderService.getStatusMap();
        Map<String, List<StatusResponse>> responseMap = new HashMap<>();
        // construct the required HashMap JSON response
        for(Map.Entry<String, Integer> entry : statusMap.entrySet()){
            for(SushiOrder order : orders){
                if(order.getStatusId().equals(entry.getValue())){
                    if(!responseMap.containsKey(entry.getKey())){
                        responseMap.put(entry.getKey(), new ArrayList<StatusResponse>());
                    }
                    responseMap.get(entry.getKey()).add(new StatusResponse(order.getId(), orderTracking.getTimeSpent(order.getId())));
                }
            }
        }
        return ResponseEntity.ok(responseMap);
    }

    @PutMapping("/orders/{order_id}/pause")
    public ResponseEntity<?> pauseOrder(@PathVariable("order_id") Long orderId) {

        try {
            orderService.pauseOrder(orderId);
            return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order paused"));
        } catch (OrderNotFoundException | OrderAlreadyFinishedException | OrderAlreadyCancelledException e) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
        }
        
    }

    @PutMapping("/orders/{order_id}/resume")
    public ResponseEntity<?> resumeOrder(@PathVariable("order_id") Long orderId){
        try {
            orderService.resumeOrder(orderId);
            return ResponseEntity.ok(new BaseResponse(BaseResponse.NORMAL_CODE, "Order resumed"));
        } catch (OrderNotFoundException | OrderNotPausedException e) {
            return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
        }
        
    }
}
