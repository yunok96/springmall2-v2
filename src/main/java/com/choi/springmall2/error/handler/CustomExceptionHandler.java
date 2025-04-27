package com.choi.springmall2.error.handler;

import com.choi.springmall2.error.exceptions.DuplicateUserException;
import com.choi.springmall2.error.exceptions.JWTExpirationException;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    // 사용자 조회 실패 예외 처리
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException ex) {
        logger.error("사용자 조회 실패: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("message", "사용자를 찾을 수 없습니다.");
        error.put("detail", ex.getMessage());
        return error;
    }

    // 중복 사용자 예외 처리
    @ExceptionHandler(DuplicateUserException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateUser(DuplicateUserException ex) {
        logger.error("중복 사용자: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("message", "이미 존재하는 사용자입니다.");
        error.put("detail", ex.getMessage());
        return error;
    }

    // 토큰 만료시 예외 처리
    @ExceptionHandler(JWTExpirationException.class)
    public ResponseEntity<?> handleJWTExpirationException(JWTExpirationException ex) {
        logger.error("JWT 만료: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 만료되었습니다. 새로운 토큰을 요청하십시오.");
    }

    // 최종 fallback 예외 처리
    @ExceptionHandler(Exception.class)
    public String handleViewException(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        logger.error("예외 발생: {}", ex.getMessage());

        int statusCode = 500; // 기본값

        // 각 예외 별 상태 코드 설정
        if (ex instanceof NoResourceFoundException noResEx) {
            statusCode = noResEx.getBody().getStatus();
        } else if (ex instanceof ResponseStatusException rsEx) {
            statusCode = rsEx.getStatusCode().value();
        } else if (ex instanceof HttpClientErrorException clientEx) {
            statusCode = clientEx.getStatusCode().value();
        } else if (ex instanceof AuthorizationDeniedException) {
            statusCode = HttpServletResponse.SC_FORBIDDEN; // 권한 없을 경우 접근 거부
        }

        // 꼭 세팅해줘야 브라우저에서도 상태 코드가 바뀜
        response.setStatus(statusCode);

        model.addAttribute("message", ex.getMessage());
        model.addAttribute("exception", ex.getClass().getSimpleName());
        model.addAttribute("status", statusCode);
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("error", ex.getMessage());

        return "error/error";
    }
}
