package com.choi.springmall2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("AccessToken 이 유효한 경우, Authentication 진행")
    void accessToken_authenticated() throws Exception {
        // given
        String validAccessToken = "validAccessToken";
        Authentication auth = mock(Authentication.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, validAccessToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken(validAccessToken)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(validAccessToken)).thenReturn(auth);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).getAuthentication(validAccessToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AccessToken 이 만료된 경우, RefreshToken 으로 AccessToken 재발급")
    void accessToken_expired_refreshToken_reissued() throws Exception {
        // given
        String expiredAccessToken = "expired";
        String validRefreshToken = "refresh";
        String newAccessToken = "newAccess";
        String newRefreshToken = "newRefresh";
        Authentication auth = mock(Authentication.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, expiredAccessToken),
                new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, validRefreshToken)
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken(expiredAccessToken)).thenReturn(false);
        when(jwtTokenProvider.validateToken(validRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(validRefreshToken)).thenReturn(auth);
        when(jwtTokenProvider.createAccessToken(auth)).thenReturn(newAccessToken);
        when(jwtTokenProvider.createRefreshToken(auth)).thenReturn(newRefreshToken);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        Cookie[] cookies = response.getCookies();
        boolean accessTokenAdded = false;
        boolean refreshTokenAdded = false;

        for (Cookie cookie : cookies) {
            if (JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()) &&
                    newAccessToken.equals(cookie.getValue())) {
                accessTokenAdded = true;
            }
            if (JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()) &&
                    newRefreshToken.equals(cookie.getValue())) {
                refreshTokenAdded = true;
            }
        }

        assert accessTokenAdded;
        assert refreshTokenAdded;
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AccessToken 이 만료되고 RefreshToken 이 만료된 경우, 에러 처리")
    void accessToken_expired_refreshToken_expired() throws Exception {
        // given
        String expiredAccessToken = "expired";
        String expiredRefreshToken = "expiredRefresh";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, expiredAccessToken),
                new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, expiredRefreshToken)
        );
        MockHttpServletResponse response = spy(new MockHttpServletResponse());

        when(jwtTokenProvider.validateToken(expiredAccessToken)).thenReturn(false);
        when(jwtTokenProvider.validateToken(expiredRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(expiredRefreshToken)).thenThrow(new RuntimeException("Token expired"));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        // 필터 체인 진행되지 않고 커스텀 에러로 포워딩 됨
        verify(filterChain, never()).doFilter(any(), any());
        assert response.getStatus() == 401;
    }
}