package com.choi.springmall2.repository;

import com.choi.springmall2.domain.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Integer> {
    Optional<DeliveryAddress> findByIdAndUserId(int id, int userId);

    List<DeliveryAddress> findByUserId(int userId);

    void deleteByIdAndUserId(int id, int userId);

    boolean existsByIdAndUserId(int id, int userId);
}
