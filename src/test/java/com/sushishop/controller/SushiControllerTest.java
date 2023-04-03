package com.sushishop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.sushishop.Constant;
import com.sushishop.entity.Sushi;
import com.sushishop.repository.SushiOrderRepository;
import com.sushishop.response.BaseResponse;
import com.sushishop.response.OrderResponse;
import com.sushishop.scheduler.OrderStatus;
import com.sushishop.service.CachedService;


@SpringBootTest
public class SushiControllerTest {

    @Autowired
    SushiController controller;

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Autowired
    CachedService cachedService;

    @Autowired
    SushiOrderRepository orderRepository;

    @Test
    @SuppressWarnings("unchecked")
    void testOrderAllSushi() {
        long i = 1L;
        while(true){
            try{
                Sushi sushi = cachedService.getSushiById(i);
                ResponseEntity<OrderResponse> response = (ResponseEntity<OrderResponse>) controller.createOrder(sushi);
                assertEquals(response.getStatusCode().value(), HttpStatus.CREATED.value());
                assertEquals(response.getBody().getCode(), BaseResponse.NORMAL_CODE);
                i++;
            }catch(Exception e){
                break;
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPauseOrder() throws InterruptedException {
        Sushi sushi = cachedService.getSushiById(1L);
        ResponseEntity<OrderResponse> response = (ResponseEntity<OrderResponse>) controller.createOrder(sushi);
        //controller.createOrder(sushi);
        //controller.createOrder(sushi);
        // pause for one second then pause the sushi
        Thread.sleep(1000);
        // test correct pause
        Long correctId = response.getBody().getOrder().getId();
        ResponseEntity<BaseResponse> response2 = (ResponseEntity<BaseResponse>) controller.pauseOrder(correctId);
        assertEquals(response2.getStatusCode().value(), HttpStatus.OK.value());
        assertEquals(response2.getBody().getCode(), BaseResponse.NORMAL_CODE);
        Thread.sleep(100);
        assertEquals(orderRepository.findById(correctId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_PAUSED));
        // test incorrect pause
        Long incorrectId = 5000L;
        ResponseEntity<BaseResponse> response3 = (ResponseEntity<BaseResponse>) controller.pauseOrder(incorrectId);
        assertEquals(response3.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(response3.getBody().getCode(), BaseResponse.ERROR_CODE);
        Thread.sleep(100);
        //assertNotEquals(orderRepository.findById(incorrectId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_PAUSED));
        assertFalse(orderRepository.findById(incorrectId).isPresent());
    }
    

    @Test
    @SuppressWarnings("unchecked")
    void testResumeOrder() throws InterruptedException {
        Sushi sushi = cachedService.getSushiById(1L);
        ResponseEntity<OrderResponse> response = (ResponseEntity<OrderResponse>) controller.createOrder(sushi);
        //controller.createOrder(sushi);
        //controller.createOrder(sushi);
        // pause for one second then pause the sushi
        Thread.sleep(1000);
        // test correct pause
        Long correctId = response.getBody().getOrder().getId();
        //Integer originalStatusId = orderRepository.findById(correctId).get().getStatusId();
        ResponseEntity<BaseResponse> response2 = (ResponseEntity<BaseResponse>) controller.pauseOrder(correctId);
        assertEquals(response2.getStatusCode().value(), HttpStatus.OK.value());
        assertEquals(response2.getBody().getCode(), BaseResponse.NORMAL_CODE);
        Thread.sleep(100);
        assertEquals(orderRepository.findById(correctId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_PAUSED));
        // test resume the order in correct scenario
        ResponseEntity<BaseResponse> response3 = (ResponseEntity<BaseResponse>) controller.resumeOrder(correctId);
        assertEquals(response3.getStatusCode().value(), HttpStatus.OK.value());
        assertEquals(response3.getBody().getCode(), BaseResponse.NORMAL_CODE);
        Thread.sleep(100);
        assertNotEquals(orderRepository.findById(correctId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_PAUSED));
        // test incorrect resume order
        Long incorrectId = 5000L;
        ResponseEntity<BaseResponse> response4 = (ResponseEntity<BaseResponse>) controller.resumeOrder(incorrectId);
        assertEquals(response4.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(response4.getBody().getCode(), BaseResponse.ERROR_CODE);
        Thread.sleep(100);
        assertFalse(orderRepository.findById(incorrectId).isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testListOrderStatus() throws InterruptedException {

        controller.createOrder(cachedService.getSushiById(1L));
        controller.createOrder(cachedService.getSushiById(2L));
        controller.createOrder(cachedService.getSushiById(3L));
        
        Thread.sleep(2000);
        ResponseEntity<Map<String, List<Map<String, Long>>>> response = (ResponseEntity<Map<String, List<Map<String, Long>>>>) controller.listOrders();
        IMap<Long, OrderStatus> orderStatusMap = hazelcastInstance.getMap("orderStatusMap");
        assertEquals(response.getStatusCode().value(), HttpStatus.OK.value());
        Map<String, List<Map<String, Long>>> map = response.getBody();
        map.forEach((key, value) -> {
            value.forEach((order) -> {
                Long orderId = order.get("orderId");
                assertTrue(orderRepository.findById(orderId).isPresent());
                // validate the status
                Integer statusId = orderRepository.findById(orderId).get().getStatusId();
                assertEquals(statusId, cachedService.getStatusIdByName(key));
                assertEquals(key, orderStatusMap.get(orderId).getStatus());
            });
        });

    }

    @Test
    @SuppressWarnings("unchecked")
    void testCancelOrder() throws InterruptedException {
        Sushi sushi = cachedService.getSushiById(1L);
        ResponseEntity<OrderResponse> response = (ResponseEntity<OrderResponse>) controller.createOrder(sushi);
        //controller.createOrder(sushi);
        //controller.createOrder(sushi);
        // pause for one second then cancel the sushi
        Thread.sleep(1000);
        // test correct pause
        Long correctId = response.getBody().getOrder().getId();
        ResponseEntity<BaseResponse> response2 = (ResponseEntity<BaseResponse>) controller.cancelOrder(correctId);
        assertEquals(response2.getStatusCode().value(), HttpStatus.OK.value());
        assertEquals(response2.getBody().getCode(), BaseResponse.NORMAL_CODE);
        Thread.sleep(100);
        assertEquals(orderRepository.findById(correctId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_CANCELLED));
        // test incorrect pause
        Long incorrectId = 5000L;
        ResponseEntity<BaseResponse> response3 = (ResponseEntity<BaseResponse>) controller.cancelOrder(incorrectId);
        assertEquals(response3.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        assertEquals(response3.getBody().getCode(), BaseResponse.ERROR_CODE);
        //Thread.sleep(100);
        //assertNotEquals(orderRepository.findById(incorrectId).get().getStatusId(), cachedService.getStatusIdByName(Constant.STATUS_CANCELLED));
        assertFalse(orderRepository.findById(incorrectId).isPresent());
    }
}
