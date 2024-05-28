package com.sushishop.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected Exception occurred", e.getMessage());
        return ResponseEntity.badRequest().body(
                Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        "error", e.getMessage()));
    }
}
