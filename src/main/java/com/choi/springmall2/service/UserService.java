package com.choi.springmall2.service;

import com.choi.springmall2.config.JwtTokenProvider;
import com.choi.springmall2.domain.Role;
import com.choi.springmall2.domain.dto.TokenDto;
import com.choi.springmall2.domain.dto.UserProfileDto;
import com.choi.springmall2.domain.dto.UserRegisterDto;
import com.choi.springmall2.domain.entity.DeliveryAddress;
import com.choi.springmall2.domain.entity.PasswordResetToken;
import com.choi.springmall2.domain.entity.User;
import com.choi.springmall2.domain.vo.DeliveryAddressVo;
import com.choi.springmall2.error.exceptions.DuplicateUserException;
import com.choi.springmall2.error.exceptions.UserNotFoundException;
import com.choi.springmall2.repository.DeliveryAddressRepository;
import com.choi.springmall2.repository.PasswordResetTokenRepository;
import com.choi.springmall2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PasswordResetTokenRepository passwordresetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // 중복 사용자 조회
    public void isEmailExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new DuplicateUserException("이미 존재하는 이메일입니다." + email);
        }
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

    // 사용자 설정 배송지 정보 조회
    public List<DeliveryAddress> getUserDeliveryAddress(int userId) {
        return deliveryAddressRepository.findByUserId(userId);
    }

    /**
     * 사용자 프로필 Dto 반환
     * @param userId 사용자 id
     * @return userProfileDto 사용자 정보 + 배송지 목록 Dto
     */
    public UserProfileDto getUserProfileDto(int userId) {
        User user = getUserById(userId);
        List<DeliveryAddress> deliveryAddresses = getUserDeliveryAddress(userId);

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setEmail(user.getEmail());
        userProfileDto.setNickname(user.getNickname());
        userProfileDto.setRole(user.getRole());
        userProfileDto.setCreateAt(user.getCreateAt());

        List<DeliveryAddressVo> deliveryAddressVos = getDeliveryAddressVos(deliveryAddresses);
        userProfileDto.setDeliveryAddresses(deliveryAddressVos);

        return userProfileDto;
    }


    /**
     * 배송지 정보 List<Vo> 매핑
     * @param deliveryAddresses 배송지 엔티티 리스트
     * @return deliveryAddressVos 배송지 vo 리스트
     */
    public List<DeliveryAddressVo> getDeliveryAddressVos(List<DeliveryAddress> deliveryAddresses) {
        if (deliveryAddresses == null || deliveryAddresses.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeliveryAddressVo> deliveryAddressVos = new ArrayList<>();
        for (DeliveryAddress deliveryAddress : deliveryAddresses) {
            DeliveryAddressVo deliveryAddressVo = new DeliveryAddressVo(
                    deliveryAddress.getRecipientName(),
                    deliveryAddress.getZipCode(),
                    deliveryAddress.getAddressLine1(),
                    deliveryAddress.getAddressLine2(),
                    deliveryAddress.getPhoneNumber(),
                    deliveryAddress.isDefault(),
                    deliveryAddress.getCreateAt()
            );
            deliveryAddressVos.add(deliveryAddressVo);
        }
        return deliveryAddressVos;
    }

    /**
     * 사용자 프로필 수정. 수정 가능한 항목은 nickName 밖에 없음
     * @param userProfileDto 사용자 Dto
     * @param userId 사용자 id
     */
    public void updateUserProfile(UserProfileDto userProfileDto, int userId) {
        User user = getUserById(userId);
        user.setNickname(userProfileDto.getNickname());
        userRepository.save(user);
    }

    /**
     * 사용자 비밀번호 초기화. 초기화 후 내용을 메일로 전송
     * @param email 사용자 이메일
     */
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 1. 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

        // 2. 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(tempPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // 3. 사용자에게 이메일 전송
    }

    /**
     * 임시 비밀번호 생성
     * @return 임시 비밀번호
     */
    private String generateTempPassword() {
        int length = 10;
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(charPool.length());
            sb.append(charPool.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * 사용자 비밀번호 초기화 토큰 생성
     * @param user 사용자 엔티티
     * @return PasswordResetToken
     */
    public PasswordResetToken createTokenForUser(User user) {
        String token = UUID.randomUUID().toString(); // 랜덤 토큰 생성
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // 1시간 유효

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpirationTime(expiryDate);

        return passwordresetTokenRepository.save(resetToken);
    }

    /**
     * 토큰 유효성 검사
     * @param token 비밀번호 초기화 토큰
     * @return 토큰 유효성 여부
     */
    public boolean isValidToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordresetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.getExpirationTime().isAfter(LocalDateTime.now());
    }


}
