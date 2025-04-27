package com.choi.springmall2.service;

import com.choi.springmall2.domain.CustomUser;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPassword(),
                user.getRole().getAuthorities()
        );
    }
}