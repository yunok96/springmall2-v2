package com.choi.springmall2.event;

public record PasswordResetMailEvent(String email, String token) {
}
