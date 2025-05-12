package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.error.exceptions.ProductDetailNotFoundException;
import com.choi.springmall2.service.ProductService;
import com.choi.springmall2.service.RedisFileKeyService;
import com.choi.springmall2.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private RedisFileKeyService redisFileKeyService;

    @Test
    @DisplayName("상품 등록 페이지가 정상적으로 렌더링되는지 확인")
    void registerProduct() throws Exception {
        String url = "/registerProduct";

        // 로그인한 사용자 설정
        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("product/registerProduct"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "SELLER")
    @DisplayName("상품 등록 처리 API 테스트")
    void registerProductSubmit() throws Exception {
        // given
        String url = "/registerProduct";

        ProductDto productDto = new ProductDto();
        productDto.setTitle("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(100.0);
        productDto.setStock(10);

        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());

        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        ProductDto savedProductDto = new ProductDto();
        savedProductDto.setTitle("Test Product");
        savedProductDto.setDescription("Test Description");
        savedProductDto.setPrice(100.0);
        savedProductDto.setStock(10);

        when(productService.saveProduct(any(ProductDto.class), eq(customUser))).thenReturn(savedProductDto);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType("application/json")
                .content("{\"title\": \"Test Product\", \"description\": \"Test Description\", \"price\": 100, \"stock\": 10}")
                .header("Authorization", "Bearer mock-token")
                .with(csrf()) // CSRF 토큰 포함
                .principal(auth) // 인증 정보도 전달
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(content().json("{\"title\": \"Test Product\", \"description\": \"Test Description\", \"price\": 100, \"stock\": 10}"));
        verify(productService, times(1)).saveProduct(any(ProductDto.class), eq(customUser));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "SELLER")
    @DisplayName("상품 등록 처리 API 예외 처리")
    void registerProductSubmit_Exception() throws Exception {
        // given
        String url = "/registerProduct";

        ProductDto productDto = new ProductDto();
        productDto.setTitle("Test Product");

        // CustomUser 객체 생성
        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());

        // CustomUser를 인증 정보로 설정
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(productService.saveProduct(any(ProductDto.class), eq(customUser))).thenThrow(new RuntimeException("상품 등록 오류"));

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType("application/json")
                .content("{\"title\": \"Test Product\"}")
                .header("Authorization", "Bearer mock-token")
                .with(csrf()) // CSRF 토큰 포함
                .principal(auth) // 인증 정보도 전달
        );

        // then
        result.andExpect(status().isInternalServerError())
                .andExpect(content().string("상품 등록 실패"));
    }

    @Test
    @DisplayName("PreSigned URL 생성 요청 처리 API 테스트")
    void getURL() throws Exception {
        // given
        String filename = "test.jpg";
        String uniqueFileName = UUID.randomUUID() + "_" + filename;
        String preSignedUrl = "https://s3.amazonaws.com/test-bucket/" + uniqueFileName;

        when(s3Service.createPreSignedUrl(anyString())).thenReturn(preSignedUrl);

        // when
        ResultActions result = mockMvc.perform(post("/getPreSignedUrl")
                .contentType("application/json")
                .content("{\"fileName\": \"" + filename + "\"}"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(preSignedUrl));
        verify(s3Service, times(1)).createPreSignedUrl(anyString());
    }

    @Test
    @DisplayName("임시 파일 키 저장 API 테스트")
    void saveTempFileKey() throws Exception {
        // given
        String fileKey = "temp-file-key";

        // when
        ResultActions result = mockMvc.perform(post("/saveTempFileKey")
                .contentType("application/json")
                .content("{\"fileKey\": \"" + fileKey + "\"}"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("파일 키 저장 성공"));
        verify(redisFileKeyService, times(1)).saveTempFileKey(fileKey);
    }

    @Test
    @DisplayName("상품 목록 조회 처리 API 예외 처리")
    void getProductList_Exception() throws Exception {
        // given
        String url = "/product/list";
        int page = 1;
        int size = 10;

        // 예외를 던지는 서비스 메서드
        when(productService.getProductsPage(page, size)).thenThrow(new RuntimeException("상품 목록 조회 실패"));

        // when
        ResultActions result = mockMvc.perform(get(url)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .with(csrf()) // CSRF 토큰 포함
        );

        // then
        result.andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("상품 목록 조회 실패")));
    }

    @Test
    @DisplayName("상품 목록 조회 정상 처리")
    void getProductList_Success() throws Exception {
        // given
        String url = "/product/list";
        int page = 1;
        int size = 10;

        // Mock 상품 목록 페이지
        Page<ProductDto> mockProductPage = mock(Page.class);
        given(mockProductPage.getTotalPages()).willReturn(5);
        given(productService.getProductsPage(page, size)).willReturn(mockProductPage);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .with(csrf()) // CSRF 토큰 포함
        );

        // then
        result.andExpect(status().isOk()) // 정상 상태 코드 확인
                .andExpect(view().name("product/list")) // 반환되는 뷰 이름 확인
                .andExpect(model().attributeExists("productPage")) // 모델에 productPage가 있는지 확인
                .andExpect(model().attribute("currentPage", page)) // currentPage가 1인지 확인
                .andExpect(model().attribute("totalPages", 5)); // totalPages가 5인지 확인

        // 서비스 메서드 호출 확인
        verify(productService, times(1)).getProductsPage(page, size);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail_Success() throws Exception {
        // given
        String url = "/product/1";
        int productId = 1;
        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setTitle("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(100.0);
        productDto.setStock(10);

        given(productService.getProductDetail(productId)).willReturn(productDto);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .with(csrf()) // CSRF 토큰 포함
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("product/detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", productDto));
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품 ID")
    void getProductDetail_Exception() throws Exception {
        // given
        String url = "/product/999";
        int invalidId = 999;
        given(productService.getProductDetail(invalidId))
                .willThrow(new ProductDetailNotFoundException("해당 상품이 존재하지 않습니다."));

        // when
        ResultActions result = mockMvc.perform(get(url)
                .with(csrf()) // CSRF 토큰 포함
        );

        // when & then
        result.andExpect(status().isNotFound())
                .andExpect(view().name("error/error"))
                .andExpect(content().string(containsString("해당 상품이 존재하지 않습니다.")))
                .andExpect(model().attribute("exception", "ProductDetailNotFoundException"))
                .andExpect(model().attribute("path", "/product/" + invalidId))
        ;
    }

}