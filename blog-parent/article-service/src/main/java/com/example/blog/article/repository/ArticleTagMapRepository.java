package com.example.blog.article.repository;

import com.example.blog.article.entity.ArticleTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagMapRepository extends JpaRepository<ArticleTagMap, String> {

    List<ArticleTagMap> findByArticleId(String articleId);

    List<ArticleTagMap> findByTagId(String tagId);

    @Modifying
    @Query("DELETE FROM ArticleTagMap m WHERE m.articleId = :articleId")
    void deleteByArticleId(@Param("articleId") String articleId);

    @Modifying
    @Query("DELETE FROM ArticleTagMap m WHERE m.tagId = :tagId")
    void deleteByTagId(@Param("tagId") String tagId);
}