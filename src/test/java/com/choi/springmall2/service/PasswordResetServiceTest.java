package com.choi.springmall2.service;

import com.choi.springmall2.domain.entity.PasswordResetToken;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.error.exceptions.ExpiredPasswordResetTokenException;
import com.choi.springmall2.error.exceptions.InvalidPasswordResetTokenException;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
import com.choi.springmall2.event.PasswordResetMailEvent;
import com.choi.springmall2.repository.PasswordResetTokenRepository;
import com.choi.springmall2.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private PasswordResetService passwordResetService;


    @Test
    @DisplayName("비밀번호 초기화 - 사용자 없음")
    void resetPassword_userNotExist() {
        // given
        String email = "notfound@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            passwordResetService.sendMailToRequestPasswordReset(email);
        });
        verify(passwordResetTokenRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("비밀번호 초기화 - 성공")
    void resetPassword_pass() {
        // given
        String email = "user@example.com";
        User user = new User();
        user.setId(1);
        user.setEmail(email);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordResetTokenRepository.existsByToken(anyString())).willReturn(false); // 토큰 중복 없음

        // when
        passwordResetService.sendMailToRequestPasswordReset(email);

        // then
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));

        verify(applicationEventPublisher).publishEvent(any(PasswordResetMailEvent.class));
    }

    @Test
    @DisplayName("비밀번호 초기화 unique 토큰 생성 - 중복될 경우")
    void generateUniqueToken_tokenExist() {
        // given
        given(passwordResetTokenRepository.existsByToken(anyString()))
                .willReturn(true)   // 첫 번째 UUID 충돌
                .willReturn(false); // 두 번째 UUID 고유값

        // when
        String token = passwordResetService.generateUniqueToken();

        // then
        assertNotNull(token);
        verify(passwordResetTokenRepository, times(2)).existsByToken(anyString());
    }

    @Test
    @DisplayName("비밀번호 초기화 unique 토큰 생성 - 성공")
    void generateUniqueToken_pass() {
        // given
        given(passwordResetTokenRepository.existsByToken(anyString())).willReturn(false);

        // when
        String token = passwordResetService.generateUniqueToken();

        // then
        assertNotNull(token);
    }

    @Test
    @DisplayName("비밀번호 초기화 unique 토큰 생성 - 존재하지 않는 토큰")
    void isValidToken_tokenNotExists() {
        // given
        String token = "test-token";
        given(passwordResetTokenRepository.findByToken(token)).willReturn(Optional.empty());

        // when
        boolean result = passwordResetService.isValidToken(token);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("비밀번호 초기화 unique 토큰 생성 - 만료된 토큰")
    void isValidToken_expiredToken() {
        // given
        String token = "test-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1));

        given(passwordResetTokenRepository.findByToken(token)).willReturn(Optional.of(resetToken));

        // when
        boolean result = passwordResetService.isValidToken(token);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("비밀번호 초기화 unique 토큰 생성 - 성공")
    void isValidToken_pass() {
        // given
        String token = "test-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        given(passwordResetTokenRepository.findByToken(token)).willReturn(Optional.of(resetToken));

        // when
        boolean result = passwordResetService.isValidToken(token);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("비밀번호 초기화 - 유효하지 않은 토큰")
    void updatePasswordWithToken_invalidToken() {
        // given
        String token = "test-token";
        String password = "new-password";

        given(passwordResetTokenRepository.findByToken(token))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidPasswordResetTokenException.class, () -> {
            passwordResetService.updatePasswordWithToken(token, password);
        });
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("비밀번호 초기화 - 만료된 토큰")
    void updatePasswordWithToken_expiredToken() {
        // given
        String token = "test-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        String password = "new-password";

        given(passwordResetTokenRepository.findByToken(token)).willReturn(Optional.of(resetToken));

        // when & then
        assertThrows(ExpiredPasswordResetTokenException.class, () -> {
            passwordResetService.updatePasswordWithToken(token, password);
        });
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("비밀번호 초기화 - 성공")
    void updatePasswordWithToken_pass() {
        // given
        User user = new User();
        user.setPassword("old-password");

        String token = "test-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUser(user);

        String password = "new-password";
        String encodedPassword = "encoded-new-password";

        given(passwordResetTokenRepository.findByToken(token)).willReturn(Optional.of(resetToken));
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);

        // when
        passwordResetService.updatePasswordWithToken(token, password);

        // then
        assertEquals(encodedPassword, user.getPassword());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

}