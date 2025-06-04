package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.DeliveryAddressRegisterDto;
import com.choi.springmall2.domain.dto.DeliveryAddressResponseDto;
import com.choi.springmall2.domain.dto.DeliveryAddressUpdateDto;
import com.choi.springmall2.service.DeliveryAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @GetMapping("/api/address/list")
    @ResponseBody
    public ResponseEntity<?> addressList(@AuthenticationPrincipal CustomUser customUser) {
        try {
            int userId = customUser.getId();
            List<DeliveryAddressResponseDto> addresses = deliveryAddressService.getDeliveryAddresses(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Error fetching delivery addresses: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "배송지 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/address/register")
    @ResponseBody
    public ResponseEntity<?> addressRegister(@RequestBody DeliveryAddressRegisterDto deliveryAddressRegisterDto
            , @AuthenticationPrincipal CustomUser customUser) {
        try {
            int userId = customUser.getId();
            deliveryAddressService.saveDeliveryAddress(deliveryAddressRegisterDto, userId);
            return ResponseEntity.ok(Map.of("message", "신규 배송지가 등록되었습니다."));
        } catch (Exception e) {
            log.error("Error registering delivery address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "배송지 등록 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/api/address/update")
    @ResponseBody
    public ResponseEntity<?> addressUpdate(@RequestBody DeliveryAddressUpdateDto deliveryAddressUpdateDto
            , @AuthenticationPrincipal CustomUser customUser) {
        try {
            int userId = customUser.getId();
            deliveryAddressService.updateDeliveryAddress(deliveryAddressUpdateDto, userId);
            return ResponseEntity.ok(Map.of("message", "배송지 수정이 완료되었습니다."));
        } catch (Exception e) {
            log.error("Error updating delivery address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "배송지 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/api/address/delete")
    @ResponseBody
    public ResponseEntity<?> addressDelete(@RequestBody DeliveryAddressUpdateDto deliveryAddressUpdateDto
            , @AuthenticationPrincipal CustomUser customUser) {
        try {
            int userId = customUser.getId();
            deliveryAddressService.deleteDeliveryAddress(deliveryAddressUpdateDto, userId);
            return ResponseEntity.ok(Map.of("message", "배송지 삭제가 완료되었습니다."));
        } catch (Exception e) {
            log.error("Error deleting delivery address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "배송지 삭제 중 오류가 발생했습니다."));
        }
    }

}
