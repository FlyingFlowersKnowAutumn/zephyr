package com.example.blog.interaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private String id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "article_id", nullable = false)
    private String articleId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "user_id", nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "parent_id")
    private String parentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "approved";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isTopLevel() {
        return parentId == null;
    }
}