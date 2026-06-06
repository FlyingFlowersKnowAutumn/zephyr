package com.example.blog.interaction.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.AnswerDTO;
import com.example.blog.common.dto.QuestionCreateDTO;
import com.example.blog.common.dto.QuestionDTO;
import com.example.blog.interaction.service.AnswerService;
import com.example.blog.interaction.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    @PostMapping
    public Result<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionCreateDTO dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "匿名") String userName) {
        return Result.success(questionService.createQuestion(dto, userId, userName));
    }

    @GetMapping
    public Result<Page<QuestionDTO>> getQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "admin".equals(role);
        return Result.success(questionService.getQuestions(status, isPublic, page, size, isAdmin));
    }

    @GetMapping("/{id}")
    public Result<QuestionDTO> getQuestionDetail(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        boolean isAdmin = "admin".equals(role);
        return Result.success(questionService.getQuestionDetail(id, userId, isAdmin));
    }

    @PutMapping("/{id}/close")
    public Result<Void> closeQuestion(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        questionService.closeQuestion(id, userId, "admin".equals(role));
        return Result.success(null);
    }

    @PostMapping("/{id}/answers")
    public Result<AnswerDTO> createAnswer(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "博主") String userName) {
        String content = body.get("content");
        return Result.success(answerService.createAnswer(id, content, userId, userName));
    }

    @PutMapping("/{id}/answers/{answerId}/accept")
    public Result<Void> acceptAnswer(
            @PathVariable String id,
            @PathVariable String answerId) {
        answerService.acceptAnswer(id, answerId);
        return Result.success(null);
    }
}
