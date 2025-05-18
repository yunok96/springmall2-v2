package com.choi.springmall2.controller;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.LoginRequestDto;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserProfileDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.service.PasswordResetService;
import com.choi.springmall2.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    // 회원가입 페이지로 이동
    @GetMapping("/signup")
    public String signup() {
        return "user/signup"; // signup.html로 이동
    }

    // 중복 이메일 확인 api
    @PostMapping("/api/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        userService.isEmailExists(email);

        return ResponseEntity.ok().build();
    }

    // 회원가입 처리 api
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto dto) {
        userService.userRegister(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto.getEmail() + " 회원가입이 완료되었습니다.");
    }

    // 로그인 페이지로 이동
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    // 로그인 처리 api
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        TokenDto tokenDto = userService.login(request.getEmail(), request.getPassword());

        // 토큰을 HttpOnly 쿠키에 저장
        ResponseCookie accessCookie = ResponseCookie.from(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, tokenDto.getAccessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(JwtTokenProvider.ACCESS_TOKEN_VALID_TIME) // 5분
                .secure(false) // TODO : HTTPS 사용 시 true
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, tokenDto.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(JwtTokenProvider.REFRESH_TOKEN_VALID_TIME) // 7일
                .secure(false) // TODO : HTTPS 사용 시 true
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("사용자 로그인 성공: {}", request.getEmail()); // 로그인 성공 로그

        return ResponseEntity.ok().build();
    }

    // 로그아웃 처리 api
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    Cookie accessTokenCookie = new Cookie(JwtTokenProvider.ACCESS_TOKEN_COOKIE_NAME, null);
                    accessTokenCookie.setHttpOnly(true);
                    accessTokenCookie.setPath("/"); // 도메인에 맞는 path 설정
                    accessTokenCookie.setMaxAge(0); // 쿠키 만료 처리
                    response.addCookie(accessTokenCookie); // 쿠키 삭제
                } else if (JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    Cookie refreshTokenCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, null);
                    refreshTokenCookie.setHttpOnly(true);
                    refreshTokenCookie.setPath("/"); // 도메인에 맞는 path 설정
                    refreshTokenCookie.setMaxAge(0); // 쿠키 만료 처리
                    response.addCookie(refreshTokenCookie); // 쿠키 삭제
                }
            }
        }

        // 로그아웃 후 리다이렉트할 URL. 없으면 "/"로 설정
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            referer = "/";
        }
        return "redirect:" + referer;
    }

    // 프로필 페이지로 이동
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUser customUser, Model model) {
        if (customUser == null) {
            return "redirect:/login";
        }

        int customUserId = customUser.getId();

        // 사용자 정보 조회
        UserProfileDto userProfileDto = userService.getUserProfileDto(customUserId);
        model.addAttribute("userProfile", userProfileDto);

        return "user/profile";
    }

    // 프로필 수정
    @PostMapping("/editProfile")
    public String editProfile(@AuthenticationPrincipal CustomUser customUser, UserProfileDto userProfileDto) {
        if (customUser == null) {
            return "redirect:/login";
        }

        int customUserId = customUser.getId();

        // 사용자 정보 수정
        userService.updateUserProfile(userProfileDto, customUserId);

        return "/";
    }

    // 비밀번호 초기화 요청
    @PostMapping("/request-password-reset")
    public String requestPasswordReset(@AuthenticationPrincipal CustomUser customUser) {
        if (customUser == null) {
            return "redirect:/login";
        }

        // TODO : 프로필 화면에 비밀번호 초기화 버튼 만든 후, 버튼을 누르면 정상적으로 동작하는지 확인.

        // 비밀번호 초기화
        passwordResetService.resetPassword(customUser.getEmail());
        return "/";
    }

    // 링크를 통해 비밀번호 재설정 요청 페이지로 이동
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, Model model) {
        boolean valid = passwordResetService.isValidToken(token);
        if (!valid) {
            model.addAttribute("message", "유효하지 않거나 만료된 토큰입니다.");
            return "error";
        }
        model.addAttribute("token", token);
        return "user/reset-password-form";
    }
}
