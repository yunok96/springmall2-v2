package com.choi.springmall2.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class CustomUser extends User {
    private int id;
    private String nickname;

    public CustomUser(int id, String email, String nickname, String password, List<GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
        this.nickname = nickname;
    }
}
