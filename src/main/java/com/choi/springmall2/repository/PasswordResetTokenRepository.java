package com.choi.springmall2.repository;

import com.choi.springmall2.domain.entity.PasswordResetToken;
import com.choi.springmall2.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);

    boolean existsByToken(String token);

    void deleteByUser(User user);
}
