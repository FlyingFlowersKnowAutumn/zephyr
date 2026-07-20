package com.example.blog.article.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_tag_map")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagMap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "article_id", nullable = false)
    private String articleId;

    @Column(name = "tag_id", nullable = false)
    private String tagId;
}