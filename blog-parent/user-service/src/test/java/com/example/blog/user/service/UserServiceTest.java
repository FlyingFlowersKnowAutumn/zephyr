package com.example.blog.user.service;

import com.example.blog.common.dto.LoginDTO;
import com.example.blog.common.dto.RegisterDTO;
import com.example.blog.common.dto.UserDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @InjectMocks private UserService userService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldRegisterSuccessfully() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(userId);
            return user;
        });

        UserDTO result = userService.register(dto);

        assertNotNull(result);
        assertEquals(userId.toString(), result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("blogger", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals(2002, ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .passwordHash("hashed")
                .role("blogger")
                .displayName("Test User")
                .build();

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(userId, "blogger")).thenReturn("access-token");
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");

        var result = userService.login(dto);

        assertNotNull(result);
        assertEquals("access-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals(userId.toString(), result.getUser().getId());
        verify(valueOperations).set(anyString(), anyString(), any());
    }

    @Test
    void shouldThrowWhenWrongPassword() {
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .passwordHash("hashed")
                .role("blogger")
                .build();

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrongpass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    void shouldGetUserById() {
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role("blogger")
                .displayName("Test")
                .bio("bio text")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(userId);
        assertEquals(userId.toString(), result.getId());
        assertEquals("Test", result.getDisplayName());
        assertEquals("bio text", result.getBio());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userService.getUserById(userId));
    }
}
