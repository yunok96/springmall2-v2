package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.vo.FileVo;
import com.choi.springmall2.service.ProductService;
import com.choi.springmall2.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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

            // 상품 등록 후, 임시 파일을 실제 파일로 이동
            s3Service.moveFromTemp(productDto.getThumbnailImage().fileKey()
            );
            for (FileVo tempContentImage : productDto.getContentImages()) {
                s3Service.moveFromTemp(tempContentImage.fileKey()
                );
            }

            return ResponseEntity.ok(savedProductDto);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생", e); // 스택트레이스를 로그에 포함
            return ResponseEntity.internalServerError().body("상품 등록 실패: " + e.getMessage());
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
}
