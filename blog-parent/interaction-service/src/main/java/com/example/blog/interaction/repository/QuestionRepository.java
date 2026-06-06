package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, String> {

    @Query("SELECT q FROM Question q WHERE (:status IS NULL OR q.status = :status) AND (:isPublic IS NULL OR q.isPublic = :isPublic) ORDER BY q.createdAt DESC")
    Page<Question> findByFilters(@Param("status") String status, @Param("isPublic") Boolean isPublic, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.status = :status ORDER BY q.createdAt DESC")
    Page<Question> findByStatus(@Param("status") String status, Pageable pageable);
}
