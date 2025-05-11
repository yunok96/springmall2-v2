package com.choi.springmall2.service;

import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.repository.ProductImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("상품 이미지 저장 테스트")
    void saveProductImages() {
        // given
        ProductDto productDto = new ProductDto();
        productDto.setThumbnailImage(new FileVo("thumbnailName", "thumbnailKey"));
        productDto.setContentImages(Arrays.asList(
                new FileVo("contentName1", "contentKey1"),
                new FileVo("contentName2", "contentKey2")
        ));
        Product product = new Product();  // 상품 객체 생성

        when(productImageRepository.save(any(ProductImage.class))).thenReturn(new ProductImage());

        // when
        List<ProductImage> savedImages = productImageService.saveProductImages(productDto, product);

        // then
        assertNotNull(savedImages);
        assertEquals(3, savedImages.size());  // 썸네일 + 내용 이미지 2개

        // verify
        verify(productImageRepository, times(3)).save(any(ProductImage.class));  // 3번 save() 호출 확인
    }

    @Test
    @DisplayName("상품 썸네일 조회 테스트 - 존재")
    void getThumbnailImage_exist() {
        // given
        ProductImage image = new ProductImage();
        image.setImageKey("key.jpg");
        image.setImageName("name.jpg");
        int productId = 1;

        when(productImageRepository.findByProductIdAndTypeOrderBySeqAsc(productId, "thumbnail")).thenReturn(List.of(image));

        // when
        FileVo result = productImageService.getThumbnailImage(productId);

        // then
        assertNotNull(result);
        assertEquals("key.jpg", result.fileKey());
        assertEquals("name.jpg", result.fileName());
    }

    @Test
    @DisplayName("상품 썸네일 조회 테스트 - 없음")
    void getThumbnailImage_none() {
        // given
        int productId = 99;

        when(productImageRepository.findByProductIdAndTypeOrderBySeqAsc(productId, "thumbnail")).thenReturn(List.of());

        // when
        FileVo result = productImageService.getThumbnailImage(productId);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("상품 내용 이미지 조회 테스트 - 존재")
    void getContentImages_exist() {
        // given
        ProductImage image1 = new ProductImage();
        image1.setImageKey("key1.jpg");
        image1.setImageName("name1.jpg");

        ProductImage image2 = new ProductImage();
        image2.setImageKey("key2.jpg");
        image2.setImageName("name2.jpg");

        int productId = 1;

        when(productImageRepository.findByProductIdAndTypeOrderBySeqAsc(productId, "content"))
                .thenReturn(List.of(image1, image2));

        // when
        List<FileVo> result = productImageService.getContentImages(productId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("key1.jpg", result.get(0).fileKey());
        assertEquals("name1.jpg", result.get(0).fileName());
        assertEquals("key2.jpg", result.get(1).fileKey());
        assertEquals("name2.jpg", result.get(1).fileName());
    }

    @Test
    @DisplayName("상품 내용 이미지 조회 테스트 - 없음")
    void getContentImages_none() {
        // given
        int productId = 99;

        when(productImageRepository.findByProductIdAndTypeOrderBySeqAsc(productId, "content")).thenReturn(List.of());

        // when
        List<FileVo> result = productImageService.getContentImages(productId);

        // then
        assertTrue(result.isEmpty());
    }
}