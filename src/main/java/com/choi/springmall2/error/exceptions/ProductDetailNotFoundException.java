package com.choi.springmall2.error.exceptions;

public class ProductDetailNotFoundException extends RuntimeException {
    public ProductDetailNotFoundException(String message) {
        super(message);
    }
}
