package com.choi.springmall2.config;

import com.choi.springmall2.domain.CustomUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Setter
    @Value("${jwt.secret}")
    private String jwtSecretKey;
    private SecretKey key;
    public static final long ACCESS_TOKEN_VALID_TIME = 60 * 5; // 액세스 토큰 유효 기간. 5분
    public static final long REFRESH_TOKEN_VALID_TIME = 60 * 60 * 24 * 7; // 리프레시 토큰 유효 기간. 7일
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey));
    }

    // Access 토큰 생성
    public String createAccessToken(Authentication auth) {
        CustomUser user = (CustomUser) auth.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (1000L * ACCESS_TOKEN_VALID_TIME));

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))

                .claim("email", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
//                .claim("roles", user.getAuthorities()) // 인증 중 에러 발생 테스트

                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // Refresh 토큰 생성
    public String createRefreshToken(Authentication auth) {
        CustomUser user = (CustomUser) auth.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (1000L * REFRESH_TOKEN_VALID_TIME));

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // 토큰 유효기간 검증
    public boolean validateToken(String token) {
        Claims claims = extractToken(token);
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("로그인 유효기간이 만료되었습니다.");
        }
        return true;
    }

    // Access 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = extractToken(token);

        int id = Integer.parseInt(claims.getSubject());
        String username = claims.get("email", String.class); // 이메일 추출
        String nickname = claims.get("nickname", String.class); // 닉네임 추출
        List<String> roles = claims.get("roles", List.class); // 권한 목록 추출

        // 권한을 SimpleGrantedAuthority로 변환
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // CustomUser 생성 (비밀번호는 토큰에 포함되지 않음)
        CustomUser principal = new CustomUser(id, username, nickname, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰에서 데이터 추출
    public Claims extractToken(String token) throws JwtException {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 토큰 형식입니다.");
        } catch (MalformedJwtException e) {
            throw new JwtException("잘못된 토큰 형식입니다.");
        } catch (SignatureException e) {
            throw new JwtException("서명 불일치");
        }
    }
}
