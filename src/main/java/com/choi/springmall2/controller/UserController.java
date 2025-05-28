package com.choi.springmall2.controller;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.dto.LoginRequestDto;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserProfileDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.error.exceptions.DuplicateUserException;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
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

    /**
     * 이메일 중복 체크
     * @param payload 이메일 JSON
     * @throws DuplicateUserException 중복된 이메일 예외
     * @return ResponseEntity
     */
    @PostMapping("/api/check-email-duplication")
    public ResponseEntity<?> checkEmailDuplication(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if ( userService.isEmailExists(email) ) {
            throw new DuplicateUserException("이미 존재하는 이메일입니다." + email);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 존재 여부 확인
     * @param payload 이메일 JSON
     * @throws UserNotFoundException 이메일이 존재하지 않는 경우 예외
     * @return ResponseEntity
     */
    @PostMapping("/api/check-email-exists")
    public ResponseEntity<?> checkEmailExists(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if ( !userService.isEmailExists(email) ) {
            throw new UserNotFoundException("가입된 이메일이 존재하지 않습니다." + email);
        }

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
        // 쿠키에서 액세스 토큰과 리프레시 토큰을 삭제함으로 로그아웃 처리
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

    /**
     * 프로필 수정
     * @param customUser 인증된 사용자 정보
     * @param userProfileDto 수정할 사용자 정보
     * @return redirect:/profile
     */
    @PostMapping("/editProfile")
    public String editProfile(@AuthenticationPrincipal CustomUser customUser, UserProfileDto userProfileDto) {
        if (customUser == null) {
            return "redirect:/login";
        }

        int customUserId = customUser.getId();

        // 사용자 정보 수정
        userService.updateUserProfile(userProfileDto, customUserId);

        return "redirect:/profile";
    }

    /**
     * 비밀번호 초기화 요청 전송
     * @param customUser 인증된 사용자 정보
     * @return redirect:/login
     */
    @PostMapping("/request-password-reset")
    @ResponseBody
    public ResponseEntity<?> requestPasswordReset(@AuthenticationPrincipal CustomUser customUser) {
        if (customUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "인증되지 않은 사용자입니다.")
            );
        }
        passwordResetService.sendMailToRequestPasswordReset(customUser.getEmail());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
    }


    /**
     * 링크를 통해 비밀번호 재설정 요청 페이지로 이동
     * @param token 비밀번호 재설정 토큰
     * @param model message | token
     * @return user/reset-password-form
     */
    @GetMapping("/reset-password")
    public String resetPassword(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("token") String token, Model model) {
        boolean valid = passwordResetService.isValidToken(token);
        if (!valid) {
            model.addAttribute("message", "유효하지 않거나 만료된 토큰입니다.");
            return "error";
        }

        // 비밀번호 수정 url 로 이동할 경우, 로그아웃 처리
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

        model.addAttribute("token", token);
        return "user/reset-password-form";
    }

    /**
     * 링크를 통해 비밀번호 재설정 요청 페이지로 이동
     * @param token 비밀번호 재설정 토큰
     * @param model message | token
     * @return ResponseEntity
     */
    @PostMapping("/reset-password-post")
    @ResponseBody
    public ResponseEntity<?> handlePasswordReset(@RequestParam("token") String token
                                    , @RequestParam("newPassword") String newPassword) {
        boolean valid = passwordResetService.isValidToken(token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "유효하지 않은 토큰입니다.")
            );
        }

        try {
            passwordResetService.updatePasswordWithToken(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "비밀번호 초기화 중 에러가 발생했습니다.")
            );
        }
    }

    /**
     * 비로그인 - 비밀번호 찾기 페이지로 이동
     * @return user/forgotPassword.html
     */
    @GetMapping("/forgotPassword")
    public String forgotPassword() {
        return "user/forgotPassword";
    }

    /**
     * 비밀번호 초기화 요청 전송
     * @param payload 이메일 JSON 데이터
     * @return redirect:/login
     */
    @PostMapping("/request-password-reset-by-email")
    @ResponseBody
    public ResponseEntity<?> requestPasswordResetByEmail(@RequestBody Map<String, String> payload) {
        if(payload == null || !payload.containsKey("email") || payload.get("email").isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일을 입력해주세요."));
        }
        String email = payload.get("email");

        passwordResetService.sendMailToRequestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
    }

}
