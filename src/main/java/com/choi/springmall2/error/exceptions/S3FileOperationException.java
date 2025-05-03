package com.choi.springmall2.error.exceptions;

public class S3FileOperationException extends RuntimeException {
    public S3FileOperationException(String message) {
        super(message);
    }
}
