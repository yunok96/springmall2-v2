package com.choi.springmall2.service;

import com.choi.springmall2.domain.entity.PasswordResetToken;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.error.exceptions.ExpiredPasswordResetTokenException;
import com.choi.springmall2.error.exceptions.InvalidPasswordResetTokenException;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
import com.choi.springmall2.event.PasswordResetMailEvent;
import com.choi.springmall2.repository.PasswordResetTokenRepository;
import com.choi.springmall2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 사용자 비밀번호 초기화 후, 이벤트 리스너를 통해 토큰을 포함한 링크를 사용자에게 메일로 전송
     * @param email 사용자 이메일
     * @throws UserNotFoundException 사용자 이메일로 사용자를 찾을 수 없을 경우
     */
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String token = generateUniqueToken();

        // 메일 전송 성공 후 DB에 저장
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpirationTime(LocalDateTime.now().plusHours(1));

        passwordResetTokenRepository.save(passwordResetToken);

        // 비밀번호 초기화 토큰 테이블 저장 후, 메일 발송 이벤트 발행
        applicationEventPublisher.publishEvent(new PasswordResetMailEvent(user.getEmail(), token));
    }

    /**
     * 비밀번호 초기화 unique 토큰 생성
     * @return 비밀번호 초기화 토큰
     */
    public String generateUniqueToken() {
        String token = UUID.randomUUID().toString();
        while (passwordResetTokenRepository.existsByToken(token)) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

    /**
     * 토큰 유효성 검사
     * @param token 비밀번호 초기화 토큰
     * @return 토큰 유효성 여부
     */
    public boolean isValidToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.getExpirationTime().isAfter(LocalDateTime.now());
    }

    /**
     * 신규 비밀번호로 사용자 비밀번호 초기화
     * @param token 비밀번호 초기화 토큰
     * @param newPassword 신규 비밀번호
     * @throws InvalidPasswordResetTokenException 유효하지 않은 토큰일 경우
     * @throws ExpiredPasswordResetTokenException 만료된 토큰일 경우
     */
    @Transactional
    public void updatePasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException("유효하지 않은 토큰입니다."));

        if (resetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ExpiredPasswordResetTokenException("토큰이 만료되었습니다.");
        }

        User user = resetToken.getUser();

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user); // @Transactional 이므로 생략해도됨 (변경감지)

        // 토큰은 재사용 방지를 위해 삭제
        passwordResetTokenRepository.delete(resetToken);
    }
}
