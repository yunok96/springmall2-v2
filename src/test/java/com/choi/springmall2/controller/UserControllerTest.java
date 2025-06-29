package com.choi.springmall2.controller;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.*;
import com.choi.springmall2.service.PasswordResetService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
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

    @MockitoBean
    PasswordResetService passwordResetService;

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
    @DisplayName("이메일 중복 확인 - 중복 이메일 존재")
    void checkEmailDuplication_duplicated() throws Exception {
        // given
        String url = "/api/check-email-duplication";

        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        given(userService.isEmailExists("test@example.com")).willReturn(true);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailJson));

        // then
        result.andExpect(status().isConflict())
                .andExpect(content().string(containsString("이미 존재하는 이메일입니다.")));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 이메일이 존재하지 않을 때")
    void checkEmailDuplication_pass() throws Exception {
        // given
        String url = "/api/check-email-duplication";

        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        given(userService.isEmailExists("test@example.com")).willReturn(false);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailJson));

        // then
        result.andExpect(status().isOk());
        verify(userService, times(1)).isEmailExists("test@example.com");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
    void checkEmailExists_notExists() throws Exception {
        // given
        String url = "/api/check-email-exists";

        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        given(userService.isEmailExists("test@example.com")).willReturn(false);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailJson));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(content().string(containsString("가입된 이메일이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재함")
    void checkEmailExists_pass() throws Exception {
        // given
        String url = "/api/check-email-exists";

        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        given(userService.isEmailExists("test@example.com")).willReturn(true);

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
                                containsString("access_token=" + accessToken),  // 쿠키 이름 변경
                                containsString("refresh_token=" + refreshToken) // 쿠키 이름 변경
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
                        containsString(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME + "=;"),
                        containsString(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME + "="),
                        containsString("Max-Age=0")
                )));
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

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("로그인된 사용자가 프로필 페이지 요청 시, 뷰를 반환한다")
    void profilePage_pass() throws Exception {
        // given
        String url = "/profile";

        // CustomUser 객체 생성 및 인증 정보로 설정
        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserProfileResponseDto dummyDto = new UserProfileResponseDto();
        dummyDto.setEmail("test@example.com");
        given(userService.getUserProfileDto(1)).willReturn(dummyDto);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("userProfile"))
        ;
    }

    @Test
    @DisplayName("익명 사용자가 프로필 수정 요청 시, /login 으로 리다이렉트")
    void editProfile_anonymous() throws Exception {
        // given
        String url = "/editProfile";

        // when
        ResultActions result = mockMvc.perform(
                post(url).param("nickname", "newNickname")
        );

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        ;
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "BUYER")
    @DisplayName("로그인된 사용자가 프로필 수정 요청 시, 프로필 수정 후 /profile 으로 리다이렉트")
    void editProfile_pass() throws Exception {
        // given
        String url = "/editProfile";

        // CustomUser 객체 생성
        CustomUser customUser = new CustomUser(1, "test@example.com", "tester", "encodedPassword", List.of());

        // CustomUser를 인증 정보로 설정
        TestingAuthenticationToken auth = new TestingAuthenticationToken(customUser, null);
        auth.setAuthenticated(true); // 인증된 사용자로 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserProfileUpdateDto dummyDto = new UserProfileUpdateDto();
        dummyDto.setNickname("newNickName");

        doNothing().when(userService).updateUserProfile(dummyDto, 1);

        // when
        ResultActions result = mockMvc.perform(
                post(url).param("nickname", "newNickName")
        );

        // then
        verify(userService).updateUserProfile(any(UserProfileUpdateDto.class), eq(1));
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
        ;
    }

    @Test
    @DisplayName("비밀번호 초기화 요청 - 유효하지 않은 토큰 입력시")
    void handlePasswordReset_invalidToken() throws Exception {
        // given
        String url = "/reset-password-post";
        String token = "test-token";
        String newPassword = "new-password";

        given(passwordResetService.isValidToken(token)).willReturn(false);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .param("token", token)
                .param("newPassword", newPassword)
        );

        // then
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
        ;
    }

    @Test
    @DisplayName("비밀번호 초기화 요청 - 비밀번호 수정 처리 중 예외 발생")
    void handlePasswordReset_exceptionDuringUpdatePassword() throws Exception {
        // given
        String url = "/reset-password-post";
        String token = "test-token";
        String newPassword = "new-password";

        given(passwordResetService.isValidToken(token)).willReturn(true);

        doThrow(new RuntimeException("비밀번호 수정 중 오류 발생"))
                .when(passwordResetService).updatePasswordWithToken(token, newPassword);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .param("token", token)
                .param("newPassword", newPassword)
        );

        // then
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("비밀번호 초기화 중 에러가 발생했습니다."))
        ;
    }

    @Test
    @DisplayName("비밀번호 초기화 요청 - 정상적으로 완료")
    void handlePasswordReset_pass() throws Exception {
        // given
        String url = "/reset-password-post";
        String token = "test-token";
        String newPassword = "new-password";

        given(passwordResetService.isValidToken(token)).willReturn(true);

        willDoNothing().given(passwordResetService).updatePasswordWithToken(token, newPassword);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .param("token", token)
                .param("newPassword", newPassword)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."))
        ;
    }

    @Test
    @DisplayName("비로그인 - 비밀번호 찾기 페이지로 이동")
    void forgotPassword() throws Exception {
        // given
        String url = "/forgotPassword";

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("비로그인 - 이메일로 비밀번호 초기화 요청 전송 - 이메일 입력 안함")
    void requestPasswordResetByEmail_emailNotInput() throws Exception {
        // given
        String url = "/request-password-reset-by-email";

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일을 입력해주세요."))
        ;
    }

    @Test
    @DisplayName("비로그인 - 이메일로 비밀번호 초기화 요청 전송 - 전송 완료")
    void requestPasswordResetByEmail_pass() throws Exception {
        // given
        String url = "/request-password-reset-by-email";
        String emailJson = """
            {
              "email": "test@example.com"
            }
            """;

        willDoNothing().given(passwordResetService).sendMailToRequestPasswordReset(emailJson);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailJson)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 링크가 이메일로 전송되었습니다."))
        ;
    }

}