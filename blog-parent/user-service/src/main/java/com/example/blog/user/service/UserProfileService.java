package com.example.blog.user.service;

import com.example.blog.common.dto.AnnouncementDTO;
import com.example.blog.common.dto.UpdateAnnouncementDTO;
import com.example.blog.common.dto.UpdateUserProfileDTO;
import com.example.blog.common.dto.UserProfileDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.User;
import com.example.blog.user.entity.UserProfile;
import com.example.blog.user.repository.UserProfileRepository;
import com.example.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);

        return mergeToDTO(user, profile);
    }

    @Transactional
    public UserProfileDTO updateProfile(UUID userId, UpdateUserProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (dto.getDisplayName() != null) user.setDisplayName(dto.getDisplayName());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        userRepository.save(user);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());

        if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());
        if (dto.getGithub() != null) profile.setGithub(dto.getGithub());
        if (dto.getTwitter() != null) profile.setTwitter(dto.getTwitter());
        if (dto.getAbout() != null) profile.setAbout(dto.getAbout());
        userProfileRepository.save(profile);

        log.info("Profile updated: userId={}", userId);
        return mergeToDTO(user, profile);
    }

    private UserProfileDTO mergeToDTO(User user, UserProfile profile) {
        return UserProfileDTO.builder()
                .userId(user.getId().toString())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .website(profile != null ? profile.getWebsite() : null)
                .github(profile != null ? profile.getGithub() : null)
                .twitter(profile != null ? profile.getTwitter() : null)
                .about(profile != null ? profile.getAbout() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public AnnouncementDTO getAnnouncement(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        if (profile != null && profile.getAnnouncementTitle() != null && profile.getAnnouncementContent() != null) {
            return AnnouncementDTO.builder()
                    .title(profile.getAnnouncementTitle())
                    .content(profile.getAnnouncementContent())
                    .build();
        }
        return AnnouncementDTO.builder()
                .title("公告")
                .content("即使再小的帆也能远航⛵")
                .build();
    }

    @Transactional
    public AnnouncementDTO updateAnnouncement(UUID userId, UpdateAnnouncementDTO dto) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());

        if (dto.getTitle() != null) profile.setAnnouncementTitle(dto.getTitle());
        if (dto.getContent() != null) profile.setAnnouncementContent(dto.getContent());
        userProfileRepository.save(profile);

        log.info("Announcement updated: userId={}", userId);
        return AnnouncementDTO.builder()
                .title(profile.getAnnouncementTitle())
                .content(profile.getAnnouncementContent())
                .build();
    }
}
