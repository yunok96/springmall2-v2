package com.choi.springmall2.error.exceptions;

public class JWTExpirationException extends RuntimeException {
    public JWTExpirationException(String message) {
        super(message);
    }
}
