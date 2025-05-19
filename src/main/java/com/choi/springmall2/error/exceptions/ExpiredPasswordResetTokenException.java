package com.choi.springmall2.error.exceptions;

public class ExpiredPasswordResetTokenException extends RuntimeException {
    public ExpiredPasswordResetTokenException(String message) {
        super(message);
    }
}
