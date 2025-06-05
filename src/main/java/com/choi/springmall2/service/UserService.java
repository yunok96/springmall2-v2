package com.choi.springmall2.service;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.Role;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserProfileResponseDto;
import com.choi.springmall2.domain.dto.UserProfileUpdateDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
import com.choi.springmall2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private final DeliveryAddressService deliveryAddressService;

    /**
     * 이메일 존재 여부 확인
     * @param email 이메일
     * @return boolean true: 존재, false: 존재하지 않음
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // 회원가입
    public void userRegister(UserRegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole(Role.valueOf(dto.getRole()));

        userRepository.save(user);
    }

    public TokenDto login(String email, String password) {
        // 사용자 인증 시도
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * repository 에서 사용자 id로 사용자 조회
     * @param id 사용자 id
     * @return User 엔티티
     * @throws UserNotFoundException HttpStatus.NOT_FOUND 코드와 JSON 응답을 반환함
     */
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자 프로필 Dto 반환
     * @param userId 사용자 id
     * @return userProfileResponseDto 사용자 정보 Dto
     */
    public UserProfileResponseDto getUserProfileDto(int userId) {
        User user = getUserById(userId);

        UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto();
        userProfileResponseDto.setEmail(user.getEmail());
        userProfileResponseDto.setNickname(user.getNickname());
        userProfileResponseDto.setRole(user.getRole());
        userProfileResponseDto.setCreateAt(user.getCreateAt());

        return userProfileResponseDto;
    }

    /**
     * 사용자 프로필 수정. 수정 가능한 항목은 nickName 밖에 없음
     * @param userProfileUpdateDto 사용자 수정 Dto
     * @param userId 사용자 id
     */
    public void updateUserProfile(UserProfileUpdateDto userProfileUpdateDto, int userId) {
        User user = getUserById(userId);
        user.setNickname(userProfileUpdateDto.getNickname());
        userRepository.save(user);
    }

}
