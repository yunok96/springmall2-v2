package com.choi.springmall2.service;

import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 저장
    public void saveProduct(Product product) {
        productRepository.save(product);
    }
}
