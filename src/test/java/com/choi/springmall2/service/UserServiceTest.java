package com.choi.springmall2.service;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.Role;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.error.exceptions.DuplicateUserException;
import com.choi.springmall2.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    // 회원가입 성공 테스트
    @Test
    @DisplayName("회원가입 성공 테스트")
    void userRegister() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("1234");
        dto.setNickname("홍길동");
        dto.setRole("ROLE_BUYER");

        String encodedPassword = "encoded_1234";
        given(passwordEncoder.encode("1234")).willReturn(encodedPassword);

        // when
        userService.userRegister(dto);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals("홍길동", savedUser.getNickname());
        assertEquals(Role.ROLE_BUYER, savedUser.getRole());
    }

    // 중복 이메일 예외 발생 테스트
    @Test
    @DisplayName("중복 이메일 예외 발생 테스트")
    void emailAlreadyExists() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("1234");
        dto.setNickname("nickname");
        dto.setRole("ROLE_BUYER");

        given(userRepository.existsByEmail("test@example.com"))
                .willReturn(true); // 👉 미리 '중복 있음'이라고 가짜 세팅

        // when
        boolean result = userService.isEmailExists("test@example.com");

        // then
        assertThat(result).isTrue();
    }

    // 비밀번호 확인 틀릴 경우 예외 발생 테스트
    @Test
    @DisplayName("비밀번호 확인 틀릴 경우 예외 발생 테스트")
    void passwordNotSame() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("1234");
        dto.setConfirmPassword("12345");
        dto.setNickname("nickname");
        dto.setRole("ROLE_BUYER");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.userRegister(dto);
        });
    }

    @Test
    @DisplayName("로그인 성공 - 토큰 발급 테스트")
    void loginSuccessTest() {
        // given
        String email = "test@example.com";
        String password = "password123";

        Authentication authentication = mock(Authentication.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        given(jwtTokenProvider.createAccessToken(authentication))
                .willReturn("mockAccessToken");

        given(jwtTokenProvider.createRefreshToken(authentication))
                .willReturn("mockRefreshToken");

        // when
        TokenDto tokenDto = userService.login(email, password);

        // then
        assertThat(tokenDto.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(tokenDto.getRefreshToken()).isEqualTo("mockRefreshToken");
    }
}