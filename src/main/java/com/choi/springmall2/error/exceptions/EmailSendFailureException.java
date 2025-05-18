package com.choi.springmall2.error.exceptions;

public class EmailSendFailureException extends RuntimeException {
    public EmailSendFailureException(String message) {
        super(message);
    }
}
