package com.example.blog.user.service;

import com.example.blog.common.dto.*;
import com.example.blog.common.enums.RoleEnum;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final VerificationCodeService verificationCodeService;

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    @Transactional
    public UserDTO register(RegisterDTO dto) {
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.conflict("用户名已存在");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.conflict("邮箱已被注册");
        }

        String role = "2806703800@qq.com".equals(dto.getEmail())
                ? RoleEnum.BLOGGER.getValue()
                : RoleEnum.USER.getValue();

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .displayName(dto.getUsername())
                .build();

        User saved = userRepository.save(user);
        verificationCodeService.deleteCode(dto.getEmail());
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());
        return convertToDTO(saved);
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        verificationCodeService.deleteCode(dto.getEmail());
        
        log.info("User password reset: id={}, email={}", user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginDTO dto) {
        String loginId = dto.getUsername();
        User user = (loginId.contains("@")
                ? userRepository.findByEmail(loginId)
                : userRepository.findByUsername(loginId))
                .orElseThrow(() -> BusinessException.unauthorized("用户名或密码错误"));

        if (user.getDeletedAt() != null) {
            throw BusinessException.unauthorized("账户已被删除");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        String token = jwtService.generateToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken();

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                user.getId().toString(),
                REFRESH_TOKEN_TTL);

        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());

        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(convertToDTO(user))
                .build();
    }

    @Transactional
    public LoginResponseDTO guestLogin(GuestLoginDTO dto) {
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> {
                    String username = generateUniqueUsername("Vent");
                    User newUser = User.builder()
                            .username(username)
                            .email(dto.getEmail())
                            .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(RoleEnum.USER.getValue())
                            .displayName("Vent")
                            .avatarUrl("/Note/mm.jpg")
                            .build();
                    User saved = userRepository.save(newUser);
                    log.info("Guest user auto-registered: id={}, username={}", saved.getId(), username);
                    return saved;
                });

        if (user.getDeletedAt() != null) {
            throw BusinessException.unauthorized("账户已被删除");
        }

        verificationCodeService.deleteCode(dto.getEmail());

        String token = jwtService.generateToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken();

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                user.getId().toString(),
                REFRESH_TOKEN_TTL);

        log.info("Guest user logged in: id={}, email={}", user.getId(), user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(convertToDTO(user))
                .build();
    }

    private String generateUniqueUsername(String base) {
        String username = base;
        Random random = new Random();
        while (userRepository.existsByUsername(username)) {
            username = base + String.format("%04d", random.nextInt(10000));
        }
        return username;
    }

    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(UUID userId) {
        return getUserById(userId);
    }

    public void logout(String refreshToken) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        log.info("User logged out, refresh token revoked");
    }

    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("旧密码错误");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw BusinessException.badRequest("新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        
        log.info("User password updated: id={}", userId);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
