package com.choi.springmall2.controller;

import com.choi.springmall2.domain.dto.ProductDto;
import com.choi.springmall2.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

//    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
//        Page<ProductDto> products = productService.getProductsPage(0, 10);
//        model.addAttribute("products", products.getContent()); // 첫 페이지의 상품 목록만 전달

        return "index";
    }
}
