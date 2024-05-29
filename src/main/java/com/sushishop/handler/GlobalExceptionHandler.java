package com.sushishop.handler;

import com.sushishop.response.BaseResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleException(Exception e) {
        log.error("Unexpected Exception occurred", e.getMessage());
        return ResponseEntity.badRequest().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found", e.getMessage());
        return ResponseEntity.unprocessableEntity().body(new BaseResponse(BaseResponse.ERROR_CODE, e.getMessage()));
    }
}
