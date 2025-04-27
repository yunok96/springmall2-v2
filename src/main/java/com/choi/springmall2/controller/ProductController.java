package com.choi.springmall2.controller;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.service.ProductService;
import com.choi.springmall2.service.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@Slf4j
public class ProductController {

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
    public String registerProduct(
            @RequestBody ProductDto productDto,
                                  Model model) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUser user = (CustomUser) auth.getPrincipal();
            // sellerId는 현재 로그인된 사용자 정보를 통해 가져와야 함
//            product.setSeller(getCurrentUserId());

//            productService.saveProduct(product);

            model.addAttribute("message", "상품 등록 성공!");
            return "redirect:/productList"; // 상품 목록 페이지로 리디렉션
        } catch (Exception e) {
            model.addAttribute("message", "상품 등록 실패: " + e.getMessage());
            return "product/registerProduct"; // 다시 등록 폼을 보여줌
        }
    }

    @GetMapping("/presignedUrlTest")
    @ResponseBody
    String getURL(@RequestParam String filename){
        String result = s3Service.createPresignedUrl("test/" + filename);
        log.info(result);
        return result;
    }



    // 현재 로그인한 사용자의 ID를 반환하는 메서드 (현재 로그인 정보는 SecurityContext에서 가져옴)
    private Long getCurrentUserId() {
        // 예시로 간단하게 "SELLER" 역할을 가진 사용자의 ID를 가져오는 코드 추가
        // 실제로는 JWT나 Spring Security 컨텍스트를 사용하여 로그인된 사용자의 ID를 가져와야 합니다.
        return 1L; // 예시 ID
    }
}
