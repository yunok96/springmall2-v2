package com.choi.springmall2.error.exceptions;

public class RedisSaveException extends RuntimeException {
    public RedisSaveException(String message) {
        super(message);
    }
}
