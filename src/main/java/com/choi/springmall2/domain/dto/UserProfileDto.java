package com.choi.springmall2.domain.dto;

import com.choi.springmall2.domain.Role;
import com.choi.springmall2.domain.vo.DeliveryAddressVo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserProfileDto {
    private String email;
    private String nickname;
    private Role role;
    private LocalDateTime createAt;

    // 사용자 배송지 필드
    List<DeliveryAddressVo> deliveryAddresses;
}