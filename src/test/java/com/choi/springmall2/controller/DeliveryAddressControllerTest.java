package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.DeliveryAddressRegisterDto;
import com.choi.springmall2.domain.dto.DeliveryAddressResponseDto;
import com.choi.springmall2.domain.dto.DeliveryAddressUpdateDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.service.DeliveryAddressService;
import com.choi.springmall2.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거
class DeliveryAddressControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryAddressService deliveryAddressService;


    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 목록 조회 - 예외 발생")
    void addressList_fail() throws Exception {
        // given
        String url = "/api/address/list";

        CustomUser customUser = new CustomUser(1, "test@example.com", "철수", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(deliveryAddressService.getDeliveryAddresses(1))
                .thenThrow(new RuntimeException("DB 오류"));

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("배송지 목록 조회 중 오류가 발생했습니다."))
        ;
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 목록 조회 - 성공")
    void addressList_pass() throws Exception {
        // given
        String url = "/api/address/list";

        CustomUser customUser = new CustomUser(1, "test@example.com", "철수", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        DeliveryAddressResponseDto dto = new DeliveryAddressResponseDto();
        dto.setRecipientName("철수");
        dto.setZipCode("12345");

        when(deliveryAddressService.getDeliveryAddresses(1))
                .thenReturn(List.of(dto));

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientName").value("철수"))
        ;
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 등록 - 성공")
    void addressRegister_pass() throws Exception {
        // given
        String url = "/api/address/register";

        CustomUser customUser = new CustomUser(1, "test@example.com", "철수", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        DeliveryAddressResponseDto dto = new DeliveryAddressResponseDto();
        dto.setRecipientName("철수");
        dto.setZipCode("12345");

        doNothing().when(deliveryAddressService).saveDeliveryAddress(any(DeliveryAddressRegisterDto.class), 1);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("신규 배송지가 등록되었습니다."))
        ;
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 수정 - 기존 배송지 없음")
    void addressUpdate_addressNotExist() throws Exception {
        // given
        String url = "/api/address/register";

        DeliveryAddressRegisterDto dto = new DeliveryAddressRegisterDto();
        dto.setRecipientName("철수 - 수정");

        CustomUser customUser = new CustomUser(1, "test@example.com", "철수", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        doThrow(new RuntimeException("에러")).when(deliveryAddressService).updateDeliveryAddress(any(DeliveryAddressUpdateDto.class), 1);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("배송지 수정 중 오류가 발생했습니다."));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 수정 - 사용자 권한 없음")
    void addressUpdate_userAccessDenied() {
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 수정 - 성공")
    void addressUpdate_pass() {
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 삭제 - 기존 배송지 없음")
    void addressDelete_addressNotExist() {
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 삭제 - 사용자 권한 없음")
    void addressDelete_userAccessDenied() {
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("배송지 삭제 - 성공")
    void addressDelete_pass() {
    }
}