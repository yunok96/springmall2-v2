package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.DeliveryAddressRegisterDto;
import com.choi.springmall2.domain.dto.DeliveryAddressResponseDto;
import com.choi.springmall2.domain.dto.DeliveryAddressUpdateDto;
import com.choi.springmall2.domain.entity.DeliveryAddress;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.repository.DeliveryAddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @InjectMocks
    private DeliveryAddressService deliveryAddressService;

    @Test
    @DisplayName("사용자 배송지 정보 조회")
    void getUserDeliveryAddress() {
        // given
        int userId = 1;
        List<DeliveryAddress> addresses = new ArrayList<>();
        given(deliveryAddressRepository.findByUserId(userId)).willReturn(addresses);

        // when
        List<DeliveryAddress> result = deliveryAddressService.getUserDeliveryAddress(userId);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("배송지 정보 List<Vo> 매핑")
    void getDeliveryAddressVos() {
        // given
        DeliveryAddress address = new DeliveryAddress();
        address.setRecipientName("홍길동");
        address.setZipCode("12345");
        address.setAddressLine1("서울시 강남구");
        address.setAddressLine2("101호");
        address.setPhoneNumber("01012345678");
        address.setDefault(true);

        List<DeliveryAddress> list = Arrays.asList(address);

        // when
        var vos = deliveryAddressService.getDeliveryAddressVos(list);

        // then
        assertEquals(1, vos.size());
        assertEquals("홍길동", vos.get(0).recipientName());
    }

    @Test
    @DisplayName("사용자 배송지 목록 조회 - 배송지 없음")
    void getDeliveryAddresses_addressNotExist() {
        // given
        int userId = 1;
        given(deliveryAddressRepository.findByUserId(userId)).willReturn(new ArrayList<>());

        // when
        List<DeliveryAddressResponseDto> result = deliveryAddressService.getDeliveryAddresses(userId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("사용자 배송지 목록 조회 - 성공")
    void getDeliveryAddresses_pass() {
        // given
        int userId = 1;

        DeliveryAddress address = new DeliveryAddress();
        address.setId(userId);
        address.setRecipientName("철수");
        address.setZipCode("54321");
        address.setAddressLine1("부산시 해운대구");
        address.setAddressLine2("202호");
        address.setPhoneNumber("01098765432");
        address.setDefault(false);

        given(deliveryAddressRepository.findByUserId(1)).willReturn(List.of(address));

        // when
        List<DeliveryAddressResponseDto> result = deliveryAddressService.getDeliveryAddresses(userId);

        // then
        assertEquals(1, result.size());
        assertEquals("철수", result.get(0).getRecipientName());
    }

    @Test
    @DisplayName("배송지 정보 저장 - 성공")
    void saveDeliveryAddress_pass() {
        // given
        DeliveryAddressRegisterDto dto = new DeliveryAddressRegisterDto();
        dto.setRecipientName("철수");
        dto.setZipCode("54321");
        dto.setAddressLine1("부산시 해운대구");
        dto.setAddressLine2("202호");
        dto.setPhoneNumber("01098765432");
        dto.setDefault(false);

        int userId = 1;

        // when
        deliveryAddressService.saveDeliveryAddress(dto, userId);

        // then
        verify(deliveryAddressRepository, times(1)).save(any(DeliveryAddress.class));
    }

    @Test
    @DisplayName("배송지 정보 수정 - 기존 배송지 조회 실패")
    void updateDeliveryAddress_previousAddressNotExist() {
        // given
        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.empty());

        int userId = 1;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> deliveryAddressService.updateDeliveryAddress(dto, userId));
    }

    @Test
    @DisplayName("배송지 정보 수정 - 접근 권한 없음")
    void updateDeliveryAddress_accessDenied() {
        // given
        User user = new User();
        user.setId(1);

        DeliveryAddress address = new DeliveryAddress();
        address.setId(1);
        address.setUser(user);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.of(address));

        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);
        dto.setRecipientName("철수-수정");
        dto.setZipCode("54321");
        dto.setAddressLine1("부산시 해운대구");
        dto.setAddressLine2("202호");
        dto.setPhoneNumber("01098765432");
        dto.setDefault(false);

        int userId = 2;

        // when & then
        assertThrows(AccessDeniedException.class, () -> deliveryAddressService.updateDeliveryAddress(dto, userId));
    }

    @Test
    @DisplayName("배송지 정보 수정 - 성공")
    void updateDeliveryAddress_pass() {
        // given
        User user = new User();
        user.setId(1);

        DeliveryAddress address = new DeliveryAddress();
        address.setId(1);
        address.setUser(user);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.of(address));

        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);
        dto.setRecipientName("철수-수정");
        dto.setZipCode("54321");
        dto.setAddressLine1("부산시 해운대구");
        dto.setAddressLine2("202호");
        dto.setPhoneNumber("01098765432");
        dto.setDefault(false);

        int userId = 1;

        // when
        deliveryAddressService.updateDeliveryAddress(dto, userId);

        // then
        verify(deliveryAddressRepository).save(address);
        assertEquals("철수-수정", address.getRecipientName());
    }

    @Test
    @DisplayName("배송지 정보 삭제 - 기존 배송지 조회 실패")
    void deleteDeliveryAddress_previousAddressNotExist() {
        // given
        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.empty());

        int userId = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> deliveryAddressService.deleteDeliveryAddress(dto, userId));
    }

    @Test
    @DisplayName("배송지 정보 삭제 - 접근 권한 없음")
    void deleteDeliveryAddress_accessDenied() {
        // given
        User user = new User();
        user.setId(1);

        DeliveryAddress address = new DeliveryAddress();
        address.setId(1);
        address.setUser(user);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.of(address));

        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);

        int userId = 2;

        // when/then
        assertThrows(AccessDeniedException.class, () -> deliveryAddressService.deleteDeliveryAddress(dto, userId));
    }

    @Test
    @DisplayName("배송지 정보 삭제 - 성공")
    void deleteDeliveryAddress_pass() {
        // given
        User user = new User();
        user.setId(1);

        DeliveryAddress address = new DeliveryAddress();
        address.setId(1);
        address.setUser(user);
        given(deliveryAddressRepository.findById(1)).willReturn(Optional.of(address));

        DeliveryAddressUpdateDto dto = new DeliveryAddressUpdateDto();
        dto.setId(1);

        int userId = 1;

        // when
        deliveryAddressService.deleteDeliveryAddress(dto, userId);

        // then
        verify(deliveryAddressRepository).delete(address);
    }

}