package com.choi.springmall2.repository;

import com.choi.springmall2.domain.entity.Product;
import com.choi.springmall2.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
