package com.example.blog.user.service;

import com.example.blog.common.dto.LoginDTO;
import com.example.blog.common.dto.LoginResponseDTO;
import com.example.blog.common.dto.RegisterDTO;
import com.example.blog.common.dto.UpdatePasswordDTO;
import com.example.blog.common.dto.UserDTO;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    @Transactional
    public UserDTO register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw BusinessException.conflict("用户名已存在");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw BusinessException.conflict("邮箱已被注册");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(RoleEnum.BLOGGER.getValue())
                .displayName(dto.getUsername())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
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

    @Transactional(readOnly = true)
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
                .isGuest(user.getIsGuest())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
