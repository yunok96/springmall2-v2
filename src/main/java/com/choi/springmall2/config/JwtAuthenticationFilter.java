package com.choi.springmall2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = getCookieValue(request, JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME);

            // 액세스 토큰이 존재하고 만료되지 않은 경우
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else { // 액세스 토큰이 없거나 만료된 경우
                String refreshToken = getCookieValue(request, JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME);

                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    try {
                        // 리프레시 토큰이 유효한 경우
                        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 새로운 액세스 토큰 발급
                        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
                        Cookie newAccessCookie = new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, newAccessToken);
                        newAccessCookie.setHttpOnly(true);
                        newAccessCookie.setPath("/");
                        newAccessCookie.setMaxAge((int) JwtTokenProvider.ACCESS_TOKEN_VALID_TIME);
                        response.addCookie(newAccessCookie);

                        // 새로운 리프레시 토큰 발급
                        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);
                        Cookie newRefreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken);
                        newRefreshCookie.setHttpOnly(true);
                        newRefreshCookie.setPath("/");
                        newRefreshCookie.setMaxAge((int) JwtTokenProvider.REFRESH_TOKEN_VALID_TIME);
                        response.addCookie(newRefreshCookie);
                    } catch (Exception e) {
                        handleErrorResponse(request, response, "세션이 만료되었습니다. 다시 로그인해주세요.", e, 401);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("토큰 처리 중 예외 발생: "+ e.getMessage());
            handleErrorResponse(request, response, "인증 토큰 처리 중 오류가 발생했습니다.", e, 401);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 요청에서 토큰 추출
    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 인증 인가 관련 오류는 필터라서 ControlAdvice 에서 처리 불가능함. 그래서 따로 처리.
    private void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, String message, Exception e, int statusCode) throws ServletException, IOException {
        request.setAttribute("message", message);
        request.setAttribute("exception", e.getClass().getSimpleName());
        request.setAttribute("status", statusCode);
        request.setAttribute("path", request.getRequestURI());
        response.setStatus(statusCode);
        request.getRequestDispatcher("/error/custom").forward(request, response);
    }
}
