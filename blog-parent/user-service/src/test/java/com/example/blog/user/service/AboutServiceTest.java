package com.example.blog.user.service;

import com.example.blog.common.dto.AboutDTO;
import com.example.blog.common.dto.UpdateAboutDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.user.entity.AboutConfig;
import com.example.blog.user.entity.User;
import com.example.blog.user.repository.AboutConfigRepository;
import com.example.blog.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AboutServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AboutConfigRepository aboutConfigRepository;
    @InjectMocks private AboutService aboutService;

    private final UUID bloggerId = UUID.randomUUID();

    @Test
    void shouldReturnDefaultWhenNoBlogger() {
        when(userRepository.findByRole("blogger")).thenReturn(List.of());

        AboutDTO result = aboutService.getAbout();

        assertEquals("", result.getContent());
        assertEquals(0, result.getSkills().length);
        assertEquals("{}", result.getSocialLinks());
    }

    @Test
    void shouldReturnDefaultWhenNoConfig() {
        User blogger = User.builder().id(bloggerId).role("blogger").build();
        when(userRepository.findByRole("blogger")).thenReturn(List.of(blogger));
        when(aboutConfigRepository.findByUserId(bloggerId)).thenReturn(Optional.empty());

        AboutDTO result = aboutService.getAbout();

        assertEquals("", result.getContent());
    }

    @Test
    void shouldReturnExistingConfig() {
        User blogger = User.builder().id(bloggerId).role("blogger").build();
        AboutConfig config = AboutConfig.builder()
                .userId(bloggerId)
                .content("Hello world")
                .skills(new String[]{"Java", "Spring"})
                .socialLinks("{\"github\":\"https://github.com/test\"}")
                .build();

        when(userRepository.findByRole("blogger")).thenReturn(List.of(blogger));
        when(aboutConfigRepository.findByUserId(bloggerId)).thenReturn(Optional.of(config));

        AboutDTO result = aboutService.getAbout();

        assertEquals("Hello world", result.getContent());
        assertEquals(2, result.getSkills().length);
        assertEquals("Java", result.getSkills()[0]);
        assertTrue(result.getSocialLinks().contains("github"));
    }

    @Test
    void shouldSaveAboutSuccessfully() {
        User blogger = User.builder().id(bloggerId).role("blogger").build();
        UpdateAboutDTO dto = UpdateAboutDTO.builder()
                .content("New content")
                .skills(new String[]{"Vue", "React"})
                .build();

        when(userRepository.findById(bloggerId)).thenReturn(Optional.of(blogger));
        when(aboutConfigRepository.findByUserId(bloggerId)).thenReturn(Optional.empty());
        when(aboutConfigRepository.save(any(AboutConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        AboutDTO result = aboutService.saveAbout(bloggerId, dto);

        assertEquals("New content", result.getContent());
        assertEquals(2, result.getSkills().length);
    }

    @Test
    void shouldRejectNonBlogger() {
        User user = User.builder().id(bloggerId).role("user").build();
        UpdateAboutDTO dto = UpdateAboutDTO.builder().content("test").build();

        when(userRepository.findById(bloggerId)).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class, () -> aboutService.saveAbout(bloggerId, dto));
        assertEquals(1003, ex.getCode());
        verify(aboutConfigRepository, never()).save(any());
    }
}
