package com.example.blog.interaction.service;

import com.example.blog.common.dto.AnswerDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.entity.Answer;
import com.example.blog.interaction.entity.Question;
import com.example.blog.interaction.repository.AnswerRepository;
import com.example.blog.interaction.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public AnswerDTO createAnswer(String questionId, String content, String userId, String userName) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.notFound("提问不存在"));

        Answer answer = Answer.builder()
                .questionId(questionId)
                .userId(userId)
                .content(content)
                .build();

        Answer saved = answerRepository.save(answer);
        log.info("Answer created: id={}, questionId={}, userId={}", saved.getId(), questionId, userId);

        return AnswerDTO.builder()
                .id(saved.getId())
                .questionId(saved.getQuestionId())
                .userId(saved.getUserId())
                .userName(userName)
                .content(saved.getContent())
                .isAccepted(saved.getIsAccepted())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void acceptAnswer(String questionId, String answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> BusinessException.notFound("回答不存在"));

        if (!answer.getQuestionId().equals(questionId)) {
            throw BusinessException.badRequest("回答不属于该提问");
        }

        answerRepository.unacceptAllForQuestion(questionId);
        answer.setIsAccepted(true);
        answerRepository.save(answer);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.notFound("提问不存在"));
        question.setStatus("answered");
        questionRepository.save(question);

        log.info("Answer accepted: questionId={}, answerId={}", questionId, answerId);
    }
}
