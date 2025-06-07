package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.DeliveryAddressRegisterDto;
import com.choi.springmall2.domain.dto.DeliveryAddressResponseDto;
import com.choi.springmall2.domain.dto.DeliveryAddressUpdateDto;
import com.choi.springmall2.domain.entity.DeliveryAddress;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;

    /**
     * 사용자 배송지 목록 DTO 반환
     * @param userId 사용자 id
     * @return responseDTOs
     */
    public List<DeliveryAddressResponseDto> getDeliveryAddresses(int userId) {
        List<DeliveryAddress> deliveryAddresses = deliveryAddressRepository.findByUserId(userId);
        if (deliveryAddresses == null || deliveryAddresses.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeliveryAddressResponseDto> responseDTOs = new ArrayList<>();
        for (DeliveryAddress deliveryAddress : deliveryAddresses) {
            DeliveryAddressResponseDto responseDTO = new DeliveryAddressResponseDto();
            responseDTO.setId(deliveryAddress.getId());
            responseDTO.setRecipientName(deliveryAddress.getRecipientName());
            responseDTO.setZipCode(deliveryAddress.getZipCode());
            responseDTO.setAddressLine1(deliveryAddress.getAddressLine1());
            responseDTO.setAddressLine2(deliveryAddress.getAddressLine2());
            responseDTO.setPhoneNumber(deliveryAddress.getPhoneNumber());
            responseDTO.setDefault(deliveryAddress.isDefault());
            responseDTO.setCreateAt(deliveryAddress.getCreateAt());

            responseDTOs.add(responseDTO);
        }
        return responseDTOs;
    }

    /**
     * 배송지 정보 저장
     * @param deliveryAddressRegisterDto 배송지 정보 등록 DTO
     */
    @Transactional
    public void saveDeliveryAddress(DeliveryAddressRegisterDto deliveryAddressRegisterDto, int userId) {
        User user = new User();
        user.setId(userId);

        DeliveryAddress deliveryAddress = new DeliveryAddress();

        deliveryAddress.setRecipientName(deliveryAddressRegisterDto.getRecipientName());
        deliveryAddress.setZipCode(deliveryAddressRegisterDto.getZipCode());
        deliveryAddress.setAddressLine1(deliveryAddressRegisterDto.getAddressLine1());
        deliveryAddress.setAddressLine2(deliveryAddressRegisterDto.getAddressLine2());
        deliveryAddress.setPhoneNumber(deliveryAddressRegisterDto.getPhoneNumber());
        deliveryAddress.setDefault(deliveryAddressRegisterDto.isDefault());
        deliveryAddress.setUser(user);

        // 해당 유저의 기존 기본배송지를 전부 해제
        if (deliveryAddressRegisterDto.isDefault()) {
            deliveryAddressRepository.clearDefaultAddress(userId);
        }

        deliveryAddressRepository.save(deliveryAddress);
    }

    /**
     * 배송지 정보 수정
     * @param deliveryAddressUpdateDto 배송지 정보 수정 DTO
     * @throws IllegalArgumentException 배송지 정보를 찾을 수 없는 경우
     */
    @Transactional
    public void updateDeliveryAddress(DeliveryAddressUpdateDto deliveryAddressUpdateDto, int userId) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        deliveryAddress.setRecipientName(deliveryAddressUpdateDto.getRecipientName());
        deliveryAddress.setZipCode(deliveryAddressUpdateDto.getZipCode());
        deliveryAddress.setAddressLine1(deliveryAddressUpdateDto.getAddressLine1());
        deliveryAddress.setAddressLine2(deliveryAddressUpdateDto.getAddressLine2());
        deliveryAddress.setPhoneNumber(deliveryAddressUpdateDto.getPhoneNumber());
        deliveryAddress.setDefault(deliveryAddressUpdateDto.isDefault());

        if ( deliveryAddress.getUser().getId() != userId) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 해당 유저의 기존 기본배송지를 전부 해제
        if (deliveryAddressUpdateDto.isDefault()) {
            deliveryAddressRepository.clearDefaultAddress(userId);
        }

        deliveryAddressRepository.save(deliveryAddress);
    }

    /**
     * 배송지 정보 삭제
     * @param deliveryAddressUpdateDto 배송지 정보 수정 DTO
     * @throws IllegalArgumentException 배송지 정보를 찾을 수 없는 경우
     */
    public void deleteDeliveryAddress(DeliveryAddressUpdateDto deliveryAddressUpdateDto, int userId) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        if ( deliveryAddress.getUser().getId() != userId) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // Check if the address is default
        boolean wasDefault = deliveryAddress.isDefault();

        // Delete the address
        deliveryAddressRepository.delete(deliveryAddress);

        // If the deleted address was default, set the first address as default
        if (wasDefault) {
            deliveryAddressRepository.findTopByUserIdOrderByIdAsc(userId)
                    .ifPresent(a -> a.setDefault(true));
        }
    }

}
