package com.sushishop.exception;

public class OrderAlreadyCancelledException extends Exception{
    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
