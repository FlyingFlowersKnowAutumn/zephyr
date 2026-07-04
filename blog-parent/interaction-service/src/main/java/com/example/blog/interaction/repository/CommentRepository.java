package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelByArticleId(@Param("articleId") UUID articleId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.articleId = :articleId AND c.deletedAt IS NULL")
    long countByArticleIdAndDeletedAtIsNull(@Param("articleId") UUID articleId);
}
