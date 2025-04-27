package com.choi.springmall2.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    private String email;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String role; // "BUYER" 또는 "SELLER"
}
