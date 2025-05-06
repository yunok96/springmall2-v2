package com.choi.springmall2.controller;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.LoginRequestDto;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@AllArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

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

    // TODO : 로그인 확인용 테스트 코드. 삭제 혹은 수정해서 사용할 예정.
    @GetMapping("/profile")
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            CustomUser user = (CustomUser) authentication.getPrincipal();
            model.addAttribute("user", authentication.getPrincipal());

            model.addAttribute("email", user.getUsername()); // 유저의 이메일을 모델에 추가
            model.addAttribute("id", user.getId()); // 유저의 이메일을 모델에 추가

            return "user/profile"; // 인증된 사용자일 경우 프로필 페이지로 이동
        } else {
            return "redirect:/login"; // 인증되지 않은 사용자일 경우 로그인 페이지로 리다이렉트
        }
    }
}
