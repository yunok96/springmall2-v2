package com.choi.springmall2.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeliveryAddressRegisterDto {
    String recipientName;
    String zipCode;
    String addressLine1;
    String addressLine2;
    String phoneNumber;
    boolean isDefault;
    LocalDateTime createAt;
}
