package com.choi.springmall2.config;

import com.choi.springmall2.domain.CustomUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// 리프레시 토큰은 유효기간만 다르니 생략
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // 각 테스트 메서드 실행 전에 호출
    @BeforeEach
    void init() {
        jwtTokenProvider = new JwtTokenProvider();

        // JWT 비밀키 생성
        SecretKey key = Jwts.SIG.HS256.key().build();
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Generated key: " + base64Key);
        jwtTokenProvider.setJwtSecretKey(base64Key);

        jwtTokenProvider.init();
    }

    // 액세스 토큰 생성
    @Test
    @DisplayName("토큰 생성 테스트")
    void createToken() {
        // given
        CustomUser user = new CustomUser(1, "test@example.com", "testnickname", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);

        // when
        String token = jwtTokenProvider.createAccessToken(authentication);

        // then
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ")); // JWT가 "eyJ"로 시작하면 정상적인 JWT임
    }

    // 유효한 토큰 인증
    @Test
    @DisplayName("유효한 토큰 인증 테스트")
    void validateToken_ValidToken() {
        // given
        CustomUser user = new CustomUser(1, "test@example.com", "testnickname", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(isValid);
    }

    // 유효하지 않은 토큰 인증
    @Test
    @DisplayName("유효하지 않은 토큰 인증 테스트")
    void validateToken_InvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.validateToken(invalidToken);
        });
    }

    // 권한 추출
    @Test
    @DisplayName("권한 추출 테스트")
    void getAuthentication() {
        // given
        CustomUser user = new CustomUser(1, "test@example.com", "testnickname", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        var result = jwtTokenProvider.getAuthentication(token);

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getName());
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    // 토큰 추출
    @Test
    @DisplayName("토큰 추출 테스트")
    void extractToken() {
        // given
        CustomUser user = new CustomUser(1, "test@example.com", "testnickname", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        Claims claims = jwtTokenProvider.extractToken(token);

        // then
        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("test@example.com", claims.get("email"));
        assertTrue(claims.get("roles", List.class).contains("ROLE_USER"));
    }
}