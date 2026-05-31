package com.example.blog.user.service;

import com.example.blog.common.dto.GuestLoginResponseDTO;
import com.example.blog.common.enums.RoleEnum;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GUEST_SESSION_PREFIX = "guest:session:";
    private static final Duration GUEST_SESSION_TTL = Duration.ofHours(24);

    @Transactional
    public GuestLoginResponseDTO guestLogin() {
        String displayName = "游客_" + UUID.randomUUID().toString().substring(0, 8);
        
        User guest = User.builder()
                .username("guest_" + UUID.randomUUID().toString().substring(0, 12))
                .displayName(displayName)
                .role(RoleEnum.GUEST.getValue())
                .isGuest(true)
                .guestExpiresAt(LocalDateTime.now().plusHours(24))
                .build();

        User saved = userRepository.save(guest);

        String token = jwtService.generateToken(saved.getId(), saved.getRole(), GUEST_SESSION_TTL);

        redisTemplate.opsForValue().set(
                GUEST_SESSION_PREFIX + saved.getId(),
                "active",
                GUEST_SESSION_TTL);

        log.info("Guest account created: id={}, displayName={}", saved.getId(), displayName);

        return GuestLoginResponseDTO.builder()
                .token(token)
                .userId(saved.getId().toString())
                .displayName(displayName)
                .role(RoleEnum.GUEST.getValue())
                .expiresAt(saved.getGuestExpiresAt())
                .build();
    }

    @Transactional
    public void deleteGuest(UUID guestId) {
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> BusinessException.notFound("游客不存在"));

        if (!guest.getIsGuest()) {
            throw BusinessException.badRequest("不是游客账户");
        }

        userRepository.delete(guest);
        redisTemplate.delete(GUEST_SESSION_PREFIX + guestId);

        log.info("Guest account deleted: id={}", guestId);
    }
}
