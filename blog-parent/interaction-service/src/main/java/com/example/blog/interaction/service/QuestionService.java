package com.example.blog.interaction.service;

import com.example.blog.common.dto.AnswerDTO;
import com.example.blog.common.dto.QuestionCreateDTO;
import com.example.blog.common.dto.QuestionDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.entity.Question;
import com.example.blog.interaction.repository.AnswerRepository;
import com.example.blog.interaction.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public QuestionDTO createQuestion(QuestionCreateDTO dto, String userId, String userName) {
        Question question = Question.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isPublic(dto.getIsPublic())
                .build();

        Question saved = questionRepository.save(question);
        log.info("Question created: id={}, userId={}", saved.getId(), userId);
        return convertToDTO(saved, userName, List.of());
    }

    @Transactional(readOnly = true)
    public Page<QuestionDTO> getQuestions(String status, Boolean isPublic, int page, int size, boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> questionPage;

        if (isAdmin) {
            questionPage = status != null ? questionRepository.findByStatus(status, pageable)
                    : questionRepository.findAll(pageable);
        } else {
            questionPage = questionRepository.findByFilters(status, isPublic != null ? isPublic : true, pageable);
        }

        return questionPage.map(q -> convertToDTO(q, null, List.of()));
    }

    @Transactional(readOnly = true)
    public QuestionDTO getQuestionDetail(String id, String userId, boolean isAdmin) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("提问不存在"));

        if (!isAdmin && !question.getIsPublic() && !question.getUserId().equals(userId)) {
            throw BusinessException.notFound("提问不存在");
        }

        List<AnswerDTO> answers = answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(id)
                .stream()
                .map(a -> AnswerDTO.builder()
                        .id(a.getId())
                        .questionId(a.getQuestionId())
                        .userId(a.getUserId())
                        .content(a.getContent())
                        .isAccepted(a.getIsAccepted())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return convertToDTO(question, null, answers);
    }

    @Transactional
    public void closeQuestion(String id, String userId, boolean isAdmin) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("提问不存在"));

        if (!isAdmin && !question.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权关闭此提问");
        }

        question.setStatus("closed");
        questionRepository.save(question);
        log.info("Question closed: id={}", id);
    }

    private QuestionDTO convertToDTO(Question question, String userName, List<AnswerDTO> answers) {
        return QuestionDTO.builder()
                .id(question.getId())
                .userId(question.getUserId())
                .userName(userName)
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus())
                .isPublic(question.getIsPublic())
                .answers(answers)
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}
