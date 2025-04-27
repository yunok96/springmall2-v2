package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.repository.ProductImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductImageService productImageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mockito 초기화
    }

    @Test
    void saveProductImages() {
        // given
        ProductDto productDto = new ProductDto();
        productDto.setThumbnailImageUrl("thumbnailUrl");
        productDto.setContentImageUrls(Arrays.asList("contentUrl1", "contentUrl2"));

        Product product = new Product();  // 상품 객체 생성

        // 이미지 저장 가짜 동작 설정
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(new ProductImage());

        // when
        List<ProductImage> savedImages = productImageService.saveProductImages(productDto, product);

        // then
        assertNotNull(savedImages);
        assertEquals(3, savedImages.size());  // 썸네일 + 내용 이미지 2개

        // verify
        verify(productImageRepository, times(3)).save(any(ProductImage.class));  // 3번 save() 호출 확인
    }
}