package com.choi.springmall2.config;

import com.choi.springmall2.error.handler.JwtAccessDeniedHandler;
import com.choi.springmall2.error.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // JWT 인증 불필요한 URL
                        .requestMatchers("/", "/signup", "/login", "/logout", "/api/login", "/api/register", "/api/check-email", "/api/refresh-token")
                        .permitAll()
                        // 정적 리소스 경로 허용
                        .requestMatchers("/js/**", "/css/**", "/images/**") // 정적 리소스 경로
                        .permitAll()
                        .anyRequest().authenticated() // 모든 요청 검증 필요
                )

                .csrf(csrf -> csrf.disable()) // TODO : 현재는 CSRF 비활성화 (개발 중 편의용)


                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // 인증되지 않은 사용자 예외 처리
                        .accessDeniedHandler(new JwtAccessDeniedHandler()) // 권한별 예외 처리
                )

                .formLogin(login -> login.disable()) // 기본 로그인 폼 비활성화. JWT 사용 시 필요 없음.
                .logout(logout -> logout.disable())// 기본 로그아웃 폼 비활성화. JWT 사용 시 필요 없음.
        ;

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
