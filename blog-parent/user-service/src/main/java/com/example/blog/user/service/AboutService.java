package com.example.blog.user.service;

import com.example.blog.common.dto.AboutDTO;
import com.example.blog.common.dto.UpdateAboutDTO;
import com.example.blog.common.enums.RoleEnum;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.AboutConfig;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.AboutConfigRepository;
import com.example.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AboutService {

    private final UserRepository userRepository;
    private final AboutConfigRepository aboutConfigRepository;

    @Transactional(readOnly = true)
    public AboutDTO getAbout() {
        List<User> bloggers = userRepository.findByRole(RoleEnum.BLOGGER.getValue());
        if (bloggers.isEmpty()) {
            return buildDefault();
        }

        UUID bloggerId = bloggers.get(0).getId();
        AboutConfig config = aboutConfigRepository.findByUserId(bloggerId).orElse(null);
        if (config == null) {
            return buildDefault();
        }

        return AboutDTO.builder()
                .content(config.getContent())
                .skills(config.getSkills())
                .socialLinks(config.getSocialLinks())
                .build();
    }

    @Transactional
    public AboutDTO saveAbout(UUID userId, UpdateAboutDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (!RoleEnum.BLOGGER.getValue().equals(user.getRole())) {
            throw BusinessException.forbidden("仅博主可编辑");
        }

        AboutConfig config = aboutConfigRepository.findByUserId(userId)
                .orElseGet(() -> AboutConfig.builder().userId(userId).build());

        if (dto.getContent() != null) config.setContent(dto.getContent());
        if (dto.getSkills() != null) config.setSkills(dto.getSkills());
        if (dto.getSocialLinks() != null) config.setSocialLinks(dto.getSocialLinks());
        aboutConfigRepository.save(config);

        log.info("About config saved: userId={}", userId);
        return AboutDTO.builder()
                .content(config.getContent())
                .skills(config.getSkills())
                .socialLinks(config.getSocialLinks())
                .build();
    }

    private AboutDTO buildDefault() {
        return AboutDTO.builder()
                .content("")
                .skills(new String[0])
                .socialLinks("{}")
                .build();
    }
}
