package com.choi.springmall2.repository;

import com.choi.springmall2.domain.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdAndTypeOrderBySeqAsc(int id, String type);
}
