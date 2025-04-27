package com.choi.springmall2.error.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    // TODO : URL 권한별 접근 제어 하려다가 여기에서 처리 안해서 놔둠. 나중에 쓸듯?
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        request.setAttribute("message", "접근 권한이 없습니다.");
        request.setAttribute("exception", accessDeniedException.getClass().getSimpleName());
        request.setAttribute("status", 403);
        request.setAttribute("path", request.getRequestURI());

        response.setStatus(403);
        request.getRequestDispatcher("/error/custom").forward(request, response);
    }
}
