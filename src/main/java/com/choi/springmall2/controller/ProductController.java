package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.service.ProductService;
import com.choi.springmall2.service.RedisFileKeyService;
import com.choi.springmall2.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    @Value("${bucket.product.temp.path}")
    private String tempProductFilePath;
    private final ProductService productService;
    private final S3Service s3Service;
    private final RedisFileKeyService redisFileKeyService;

    @GetMapping("/registerProduct")
    @PreAuthorize("hasRole('SELLER')")
    public String registerProduct() {
        return "product/registerProduct";
    }

    // 상품 등록 처리
    @PostMapping("/registerProduct")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseBody
    public ResponseEntity<?> registerProductSubmit(
            @RequestBody ProductDto productDto,
            Authentication authentication) {
        try {
            CustomUser customUser = (CustomUser) authentication.getPrincipal();

            ProductDto savedProductDto = productService.saveProduct(productDto, customUser); // CustomUser 전달

            FileVo thumbnailImage = productDto.getThumbnailImage();
            if (thumbnailImage != null) {
                s3Service.moveFromTemp(thumbnailImage.fileKey()); // 상품 등록 후, 임시 파일을 실제 파일로 이동
            }

            List<FileVo> contentImages = productDto.getContentImages();
            if (contentImages != null) {
                for (FileVo tempContentImage : contentImages) {
                    s3Service.moveFromTemp(tempContentImage.fileKey()); // 상품 등록 후, 임시 파일을 실제 파일로 이동
                }
            }

            return ResponseEntity.ok(savedProductDto);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생", e); // 스택트레이스를 로그에 포함
            return ResponseEntity.internalServerError().body("상품 등록 실패");
        }
    }

    // 등록 전, 임시 파일 PreSigned URL 생성 요청 처리
    @PostMapping("/getPreSignedUrl")
    @ResponseBody
    public Map<String, String> getURL(@RequestBody Map<String, String> payload) {
        String filename = payload.get("fileName");
        String uniqueFileName = UUID.randomUUID() + "_" + filename;
        String url = s3Service.createPreSignedUrl(tempProductFilePath + uniqueFileName);

        return Map.of("url", url);
    }

    // 임시 등록한 file key 를 Redis 에 저장
    @PostMapping("/saveTempFileKey")
    @ResponseBody
    public ResponseEntity<?> saveTempFileKey(@RequestBody Map<String, String> payload) {
        String fileKey = payload.get("fileKey");
        redisFileKeyService.saveTempFileKey(fileKey); // Redis 에 저장

        return ResponseEntity.ok("파일 키 저장 성공");
    }

    // 상품 목록 조회
    @GetMapping("/product/list")
    public String getProductList(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size) {
        // 상품 목록 조회
        Page<ProductDto> productPage = productService.getProductsPage(page, size);

        // 모델에 데이터 추가
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "product/list"; // Thymeleaf 템플릿 (product/list.html)로 반환
    }


}
