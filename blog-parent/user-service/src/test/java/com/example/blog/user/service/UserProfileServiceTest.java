package com.example.blog.user.service;

import com.example.blog.common.dto.UpdateUserProfileDTO;
import com.example.blog.common.dto.UserProfileDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.entity.UserProfile;
import com.example.blog.user.repository.UserProfileRepository;
import com.example.blog.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @InjectMocks private UserProfileService userProfileService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldGetProfileWithExistingUserAndProfile() {
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test")
                .bio("bio")
                .avatarUrl("https://avatar.url")
                .build();
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .website("https://example.com")
                .github("https://github.com/test")
                .about("about text")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        UserProfileDTO result = userProfileService.getProfile(userId);

        assertEquals(userId.toString(), result.getUserId());
        assertEquals("Test", result.getDisplayName());
        assertEquals("bio", result.getBio());
        assertEquals("https://avatar.url", result.getAvatarUrl());
        assertEquals("https://example.com", result.getWebsite());
        assertEquals("https://github.com/test", result.getGithub());
        assertEquals("about text", result.getAbout());
    }

    @Test
    void shouldGetProfileWithNoProfileRecord() {
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        UserProfileDTO result = userProfileService.getProfile(userId);

        assertNotNull(result);
        assertEquals("Test", result.getDisplayName());
        assertNull(result.getWebsite());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userProfileService.getProfile(userId));
    }

    @Test
    void shouldUpsertProfileOnFirstUpdate() {
        User user = User.builder().id(userId).username("testuser").build();
        UpdateUserProfileDTO dto = UpdateUserProfileDTO.builder()
                .displayName("New Name")
                .website("https://new.site")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDTO result = userProfileService.updateProfile(userId, dto);

        assertEquals("New Name", result.getDisplayName());
        assertEquals("https://new.site", result.getWebsite());
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateExistingProfile() {
        User user = User.builder().id(userId).username("testuser").displayName("Old").build();
        UserProfile existingProfile = UserProfile.builder().userId(userId).website("https://old.site").build();
        UpdateUserProfileDTO dto = UpdateUserProfileDTO.builder()
                .displayName("Updated")
                .about("new about")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(userProfileRepository.save(existingProfile)).thenReturn(existingProfile);

        UserProfileDTO result = userProfileService.updateProfile(userId, dto);

        assertEquals("Updated", result.getDisplayName());
        assertEquals("https://old.site", result.getWebsite());
        assertEquals("new about", result.getAbout());
    }
}
