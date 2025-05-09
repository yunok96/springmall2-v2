package com.choi.springmall2.controller;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.LoginRequestDto;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("회원가입 페이지로 이동")
    void signup() throws Exception {
        // given
        String url = "/signup";

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 중복 확인")
    void checkEmail() throws Exception {
        // given
        String url = "/api/check-email";

        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        doNothing().when(userService).isEmailExists("test@example.com"); // 호출 시 예외가 발생하지 않도록 설정

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailJson));

        // then
        result.andExpect(status().isOk());
        verify(userService, times(1)).isEmailExists("test@example.com");
    }

    @Test
    @DisplayName("회원가입 API 테스트")
    void registerUser() throws Exception {
        // given
        String url = "/api/register";

        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setNickname("tester");

        doNothing().when(userService).userRegister(any(UserRegisterDto.class));

        // when
        ResultActions result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(content().string("test@example.com 회원가입이 완료되었습니다."));
        verify(userService, times(1)).userRegister(any(UserRegisterDto.class));

    }

    @Test
    @DisplayName("로그인 페이지로 이동")
    void login() throws Exception {
        // given
        String url = "/login";

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 API 테스트")
    void apiLogin() throws Exception {
        // given
        String url = "/api/login";
        String email = "test@example.com";
        String password = "password123";

        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail(email);
        requestDto.setPassword(password);

        String accessToken = "mock-access-token";
        String refreshToken = "mock-refresh-token";
        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);

        // userService.login(email, password) 호출 시 tokenDto 리턴하도록 설정
        when(userService.login(eq(email), eq(password))).thenReturn(tokenDto);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        Matchers.hasItems(
                                Matchers.containsString("access_token=" + accessToken),  // 쿠키 이름 변경
                                Matchers.containsString("refresh_token=" + refreshToken) // 쿠키 이름 변경
                        )
                ));

        verify(userService, times(1)).login(eq(email), eq(password));
    }

    @Test
    @DisplayName("로그아웃 시 accessToken, refreshToken 쿠키가 삭제되고 리다이렉트된다")
    void logout_Success_DeletesCookiesAndRedirects() throws Exception {
        // given
        String url = "/logout";
        Cookie accessToken = new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, "access-token");
        Cookie refreshToken = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "refresh-token");

        String referer = "/mypage";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .cookie(accessToken, refreshToken)
                .header("Referer", referer));

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(referer))
                .andExpect(header().stringValues("Set-Cookie", Matchers.hasItems(
                        Matchers.containsString(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME + "=;"),
                        Matchers.containsString(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME + "="),
                        Matchers.containsString("Max-Age=0")
                )));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("로그인된 사용자가 프로필 페이지 요청 시, 뷰를 반환한다")
    void profilePage_auth() throws Exception {
        // given
        String url = "/profile";

        // CustomUser 객체 생성
        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());

        // CustomUser를 인증 정보로 설정
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attribute("email", "test@example.com"))
                .andExpect(model().attribute("id", 1));
    }

    @Test
    @DisplayName("비로그인 사용자가 프로필 페이지 요청 시, /login으로 리다이렉트 된다")
    void profilePage_anonymous() throws Exception {
        // given
        String url = "/profile";

        SecurityContextHolder.clearContext(); // 익명 사용자

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}