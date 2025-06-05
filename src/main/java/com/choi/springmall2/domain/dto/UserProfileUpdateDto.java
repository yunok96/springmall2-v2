package com.choi.springmall2.domain.dto;

import com.choi.springmall2.domain.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileUpdateDto {
    private int id;
    private String nickname;
    private Role role;
    private LocalDateTime createAt;
}