package com.choi.springmall2.service;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageService productImageService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mockito 초기화
    }

    @Test
    void saveProduct() {
        // given
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(100.0);
        productDto.setStock(10);
        productDto.setThumbnailImage(new FileVo("thumbnailName", "thumbnailKey"));
        productDto.setContentImages(Arrays.asList(
                new FileVo("contentName1", "contentKey1"),
                new FileVo("contentName2", "contentKey2")
        ));

        Product product = new Product();
        product.setTitle("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setStock(10);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductImage mockThumbnailImage = new ProductImage();
        mockThumbnailImage.setImageKey("thumbnailKey");
        mockThumbnailImage.setImageName("thumbnailName");

        ProductImage mockContentImage1 = new ProductImage();
        mockContentImage1.setImageKey("contentKey1");
        mockContentImage1.setImageName("contentName1");

        ProductImage mockContentImage2 = new ProductImage();
        mockContentImage2.setImageKey("contentKey2");
        mockContentImage2.setImageName("contentName2");

        List<ProductImage> mockProductImages = Arrays.asList(mockThumbnailImage, mockContentImage1, mockContentImage2);
        when(productImageService.saveProductImages(any(ProductDto.class), any(Product.class))).thenReturn(mockProductImages);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_TEST"));
        CustomUser customUser = new CustomUser(
                999,
                "testuser@test.com",
                "테스트유저",
                "testPassword",
                authorities
        );
        when(userService.getUserById(999)).thenReturn(any(User.class));

        // when
        ProductDto result = productService.saveProduct(productDto, customUser);

        // then
        assertNotNull(result);
        assertEquals("Test Product", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(100.0, result.getPrice());
        assertEquals(10, result.getStock());
        assertEquals(new FileVo(mockThumbnailImage.getImageName(), mockThumbnailImage.getImageKey()), result.getThumbnailImage());
        assertEquals(2, result.getContentImages().size());  // content 이미지가 2개 있는지 체크

        // verify
        verify(productRepository, times(1)).save(any(Product.class));  // 상품 저장 확인
    }
}