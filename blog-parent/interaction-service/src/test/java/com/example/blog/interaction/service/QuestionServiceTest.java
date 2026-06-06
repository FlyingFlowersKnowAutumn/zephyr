package com.example.blog.interaction.service;

import com.example.blog.common.dto.QuestionCreateDTO;
import com.example.blog.common.dto.QuestionDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.entity.Question;
import com.example.blog.interaction.repository.AnswerRepository;
import com.example.blog.interaction.repository.QuestionRepository;
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
class QuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerRepository answerRepository;
    @InjectMocks private QuestionService questionService;

    @Test
    void shouldCreateQuestion() {
        QuestionCreateDTO dto = new QuestionCreateDTO();
        dto.setTitle("How to test?");
        dto.setContent("I need help with testing");
        dto.setIsPublic(true);

        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(UUID.randomUUID().toString());
            return q;
        });

        QuestionDTO result = questionService.createQuestion(dto, "user-1", "User");

        assertNotNull(result);
        assertEquals("How to test?", result.getTitle());
        assertEquals("user-1", result.getUserId());
        assertTrue(result.isPublic());
    }

    @Test
    void shouldThrowWhenQuestionNotFound() {
        when(questionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> questionService.getQuestionDetail("missing", "user-1", false));
    }

    @Test
    void shouldCloseQuestion() {
        Question question = Question.builder()
                .id("q-1")
                .userId("user-1")
                .title("Test")
                .content("Content")
                .isPublic(true)
                .build();

        when(questionRepository.findById("q-1")).thenReturn(Optional.of(question));

        questionService.closeQuestion("q-1", "user-1", false);

        assertEquals("closed", question.getStatus());
        verify(questionRepository).save(question);
    }

    @Test
    void shouldRejectCloseByNonOwner() {
        Question question = Question.builder()
                .id("q-1")
                .userId("owner")
                .title("Test")
                .build();

        when(questionRepository.findById("q-1")).thenReturn(Optional.of(question));

        assertThrows(BusinessException.class,
                () -> questionService.closeQuestion("q-1", "other-user", false));
    }
}
