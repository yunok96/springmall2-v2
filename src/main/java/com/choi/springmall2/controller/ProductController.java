package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.service.ProductService;
import com.choi.springmall2.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    @Value("${bucket.product.temp.path}")
    private String tempProductFilePath;
    @Value("${bucket.product.real.path}")
    private String realProductFilePath;
    private final ProductService productService;
    private final S3Service s3Service;

    @GetMapping("/registerProduct")
    @PreAuthorize("hasRole('SELLER')")
    public String registerProduct() {
        return "product/registerProduct";
    }

    // 상품 등록 처리 (Presigned URL 방식)
    @PostMapping("/registerProduct")
    @PreAuthorize("hasRole('SELLER')")
    public String registerProductSubmit(
            @ModelAttribute ProductDto productDto,
            @RequestParam("contentImageUrls") String contentImageUrlsJson, // JSON 형태의 이미지 URL 리스트
            Authentication authentication,
            Model model) {
        try {
            CustomUser customUser = (CustomUser) authentication.getPrincipal();

            // JSON 형태의 contentImageUrls를 List<String>으로 변환
            List<String> contentImageUrls = Arrays.stream(contentImageUrlsJson.substring(1, contentImageUrlsJson.length() - 1).split(","))
                    .map(s -> s.trim().replaceAll("\"", ""))
                    .collect(Collectors.toList());
            productDto.setContentImageUrls(contentImageUrls);

            ProductDto savedProductDto = productService.saveProduct(productDto, customUser); // CustomUser 전달

            // 상품 등록 후, 임시 파일을 실제 파일로 이동
            for (String tempImageUrl : contentImageUrls) {
                String filename = tempImageUrl.substring(tempImageUrl.lastIndexOf("/") + 1);
                s3Service.moveFromTemp(tempProductFilePath + filename, realProductFilePath + filename);
            }
            String tempThumbFilename = productDto.getThumbnailImageUrl()
                    .substring(productDto.getThumbnailImageUrl().lastIndexOf("/") + 1);
            s3Service.moveFromTemp(tempProductFilePath + tempThumbFilename, realProductFilePath + tempThumbFilename);

            model.addAttribute("message", "상품 등록 성공!");
            return "redirect:/productList"; // 상품 목록 페이지로 리디렉션
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생", e); // 스택트레이스를 로그에 포함
            model.addAttribute("message", "상품 등록 실패: " + e.getMessage());
            return "product/registerProduct"; // 다시 등록 폼을 보여줌
        }
    }

    // 등록 전, 임시 파일 PreSigned URL 생성 요청 처리
    @GetMapping("/getPreSignedUrl")
    @ResponseBody
    public Map<String, String> getURL(@RequestParam String filename) {
        String url = s3Service.createPreSignedUrl(tempProductFilePath + filename);
        return Map.of("url", url);
    }
}
