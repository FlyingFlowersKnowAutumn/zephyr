package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, String> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(String questionId);

    @Query("SELECT a FROM Answer a WHERE a.questionId = :questionId AND a.isAccepted = true AND a.deletedAt IS NULL")
    List<Answer> findAcceptedByQuestionId(@Param("questionId") String questionId);

    @Modifying
    @Query("UPDATE Answer a SET a.isAccepted = false WHERE a.questionId = :questionId AND a.isAccepted = true")
    void unacceptAllForQuestion(@Param("questionId") String questionId);
}
