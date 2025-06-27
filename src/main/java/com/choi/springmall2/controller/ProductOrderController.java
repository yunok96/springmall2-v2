package com.choi.springmall2.controller;

import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductOrderController {

//    private final ProductService productService;

    @GetMapping("/productOrder")
    public String productOrder(
            @RequestParam int productId,
            @RequestParam int quantity,
            Model model) {
//        Product product = productService.findById(productId);

//        double totalPrice = product.getPrice() * quantity; // 서버에서 계산

        model.addAttribute("productId", productId);
        model.addAttribute("quantity", quantity);

        return "productOrder/productOrder";
    }

}
