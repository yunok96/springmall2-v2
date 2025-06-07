package com.choi.springmall2.repository;

import com.choi.springmall2.domain.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Integer> {

    List<DeliveryAddress> findByUserId(int userId);

    @Modifying
    @Query("UPDATE DeliveryAddress d SET d.isDefault = false WHERE d.user.id = :userId AND d.isDefault = true")
    void clearDefaultAddress(@Param("userId") int userId);

    Optional<DeliveryAddress> findTopByUserIdOrderByIdAsc(int userId);
}
