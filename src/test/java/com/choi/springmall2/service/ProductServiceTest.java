package com.choi.springmall2.service;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.ProductImage;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.error.exceptions.ProductDetailNotFoundException;
import com.choi.springmall2.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Test
    @DisplayName("상품 목록 페이지 조회")
    void getProductsPage() {
        // given
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Product product1 = new Product();
        product1.setId(1);
        product1.setTitle("상품1");
        product1.setDescription("설명1");
        product1.setPrice(1000.0);
        product1.setStock(10);
        
        Product product2 = new Product();
        product2.setId(2);
        product2.setTitle("상품2");
        product2.setDescription("설명2");
        product2.setPrice(2000.0);
        product2.setStock(5);
        
        List<Product> products = List.of(product1, product2);

        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        given(productRepository.findAll(pageable)).willReturn(productPage);
        given(productImageService.getThumbnailImage(1)).willReturn(new FileVo("img1.jpg", "key1"));
        given(productImageService.getThumbnailImage(2)).willReturn(new FileVo("img2.jpg", "key2"));

        // when
        Page<ProductDto> result = productService.getProductsPage(page, size);

        // then
        assertEquals(2, result.getContent().size());

        ProductDto dto1 = result.getContent().get(0);
        assertEquals(1, dto1.getId());
        assertEquals("상품1", dto1.getTitle());
        assertEquals("설명1", dto1.getDescription());
        assertEquals(1000, dto1.getPrice());
        assertEquals(10, dto1.getStock());
        assertNotNull(dto1.getThumbnailImage());
        assertEquals("img1.jpg", dto1.getThumbnailImage().fileName());

        ProductDto dto2 = result.getContent().get(1);
        assertEquals("상품2", dto2.getTitle());
        assertEquals("img2.jpg", dto2.getThumbnailImage().fileName());
    }

    @Test
    @DisplayName("상품 상세 조회 - 성공")
    void getProductDetail() {
        // given
        int productId = 1;
        Product product = new Product();
        product.setId(productId);
        product.setTitle("상품1");
        product.setDescription("설명1");
        product.setPrice(1000.0);
        product.setStock(10);

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));
        when(productImageService.getThumbnailImage(productId)).thenReturn(new FileVo("img1.jpg", "key1"));
        when(productImageService.getContentImages(productId)).thenReturn(List.of(new FileVo("img2.jpg", "key2")));

        // when
        ProductDto result = productService.getProductDetail(productId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("상품1", result.getTitle());
        assertEquals("설명1", result.getDescription());
        assertEquals(1000, result.getPrice());
        assertEquals(10, result.getStock());
        assertNotNull(result.getThumbnailImage());
        assertEquals("img1.jpg", result.getThumbnailImage().fileName());
    }

    @Test
    @DisplayName("상품 상세 조회 - 실패")
    void getProductDetail_fail() {
        // given
        int productId = 1;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ProductDetailNotFoundException.class, () -> {
            productService.getProductDetail(productId); // 예외 발생 예상
        });
    }
}