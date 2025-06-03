package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.DeliveryAddressRegisterDto;
import com.choi.springmall2.domain.dto.DeliveryAddressResponseDto;
import com.choi.springmall2.domain.dto.DeliveryAddressUpdateDto;
import com.choi.springmall2.domain.entity.DeliveryAddress;
import com.choi.springmall2.domain.vo.DeliveryAddressVo;
import com.choi.springmall2.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;

    // 사용자 설정 배송지 정보 조회
    public List<DeliveryAddress> getUserDeliveryAddress(int userId) {
        return deliveryAddressRepository.findByUserId(userId);
    }

    /**
     * 배송지 정보 List<Vo> 매핑
     * @param deliveryAddresses 배송지 엔티티 리스트
     * @return deliveryAddressVos 배송지 vo 리스트
     */
    public List<DeliveryAddressVo> getDeliveryAddressVos(List<DeliveryAddress> deliveryAddresses) {
        if (deliveryAddresses == null || deliveryAddresses.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeliveryAddressVo> deliveryAddressVos = new ArrayList<>();
        for (DeliveryAddress deliveryAddress : deliveryAddresses) {
            DeliveryAddressVo deliveryAddressVo = new DeliveryAddressVo(
                    deliveryAddress.getRecipientName(),
                    deliveryAddress.getZipCode(),
                    deliveryAddress.getAddressLine1(),
                    deliveryAddress.getAddressLine2(),
                    deliveryAddress.getPhoneNumber(),
                    deliveryAddress.isDefault(),
                    deliveryAddress.getCreateAt()
            );
            deliveryAddressVos.add(deliveryAddressVo);
        }
        return deliveryAddressVos;
    }

    public List<DeliveryAddressResponseDto> getDeliveryAddresses(int userId) {
        List<DeliveryAddress> deliveryAddresses = deliveryAddressRepository.findByUserId(userId);
        if (deliveryAddresses == null || deliveryAddresses.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeliveryAddressResponseDto> responseDtos = new ArrayList<>();
        for (DeliveryAddress deliveryAddress : deliveryAddresses) {


            // TODO : Implement logic for mapping Entity to Dto
//            DeliveryAddressResponseDto responseDto = new DeliveryAddressResponseDto(
//                    deliveryAddress.getId(),
//                    deliveryAddress.getRecipientName(),
//                    deliveryAddress.getZipCode(),
//                    deliveryAddress.getAddressLine1(),
//                    deliveryAddress.getAddressLine2(),
//                    deliveryAddress.getPhoneNumber(),
//                    deliveryAddress.isDefault()
//            );
//            responseDtos.add(responseDto);
        }
        return responseDtos;

    }

    /**
     * 배송지 정보 저장
     * @param deliveryAddressRegisterDto 배송지 정보 등록 DTO
     */
    public void saveDeliveryAddress(DeliveryAddressRegisterDto deliveryAddressRegisterDto) {
        DeliveryAddress deliveryAddress = new DeliveryAddress();

        deliveryAddress.setRecipientName(deliveryAddressRegisterDto.getRecipientName());
        deliveryAddress.setZipCode(deliveryAddressRegisterDto.getZipCode());
        deliveryAddress.setAddressLine1(deliveryAddressRegisterDto.getAddressLine1());
        deliveryAddress.setAddressLine2(deliveryAddressRegisterDto.getAddressLine2());
        deliveryAddress.setPhoneNumber(deliveryAddressRegisterDto.getPhoneNumber());
        deliveryAddress.setDefault(deliveryAddressRegisterDto.isDefault());

        deliveryAddressRepository.save(deliveryAddress);
    }

    /**
     * 배송지 정보 수정
     * @param deliveryAddressUpdateDto 배송지 정보 수정 DTO
     * @throws IllegalArgumentException 배송지 정보를 찾을 수 없는 경우
     */
    public void updateDeliveryAddress(DeliveryAddressUpdateDto deliveryAddressUpdateDto) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        deliveryAddress.setRecipientName(deliveryAddressUpdateDto.getRecipientName());
        deliveryAddress.setZipCode(deliveryAddressUpdateDto.getZipCode());
        deliveryAddress.setAddressLine1(deliveryAddressUpdateDto.getAddressLine1());
        deliveryAddress.setAddressLine2(deliveryAddressUpdateDto.getAddressLine2());
        deliveryAddress.setPhoneNumber(deliveryAddressUpdateDto.getPhoneNumber());
        deliveryAddress.setDefault(deliveryAddressUpdateDto.isDefault());

        deliveryAddressRepository.save(deliveryAddress);
    }

    /**
     * 배송지 정보 삭제
     * @param deliveryAddressUpdateDto 배송지 정보 수정 DTO
     * @throws IllegalArgumentException 배송지 정보를 찾을 수 없는 경우
     */
    public void deleteDeliveryAddress(DeliveryAddressUpdateDto deliveryAddressUpdateDto) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        deliveryAddressRepository.delete(deliveryAddress);
    }

}
