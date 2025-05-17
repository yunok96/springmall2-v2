package com.choi.springmall2.domain.vo;

import java.time.LocalDateTime;

public record DeliveryAddressVo(
        String recipientName,
        String zipCode,
        String addressLine1,
        String addressLine2,
        String phoneNumber,
        boolean isDefault,
        LocalDateTime createAt
) {
}
