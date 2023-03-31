package com.sushishop.exception;

public class OrderAlreadyFinishedException extends Exception {
    public OrderAlreadyFinishedException(String message) {
        super(message);
    }
}