package com.example.blog.article.repository;

import com.example.blog.article.entity.Article;
import com.example.blog.article.entity.Article.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
    
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    
    Page<Article> findByAuthorId(String authorId, Pageable pageable);
    
    Page<Article> findByAuthorIdAndStatus(String authorId, ArticleStatus status, Pageable pageable);
    
    Page<Article> findByCategoryId(String categoryId, Pageable pageable);
    
    Page<Article> findByCategoryIdAndStatus(String categoryId, ArticleStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = :status AND a.publishedAt IS NOT NULL ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedArticles(@Param("status") ArticleStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = :status AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> searchByTitle(@Param("keyword") String keyword, @Param("status") ArticleStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = :status ORDER BY a.createdAt DESC")
    Page<Article> findRecentArticles(@Param("status") ArticleStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT a.categoryName FROM Article a WHERE a.categoryName IS NOT NULL ORDER BY a.categoryName")
    List<String> findAllCategoryNames();
    
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    int incrementViewCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    int incrementLikeCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Article a SET a.commentCount = a.commentCount + 1 WHERE a.id = :id")
    int incrementCommentCount(@Param("id") String id);
    
    @Query(value = "SELECT a.* FROM articles a JOIN article_tags t ON CAST(a.id AS VARCHAR) = t.article_id WHERE t.tag = :tag AND a.status = :status ORDER BY a.published_at DESC",
           countQuery = "SELECT COUNT(DISTINCT a.id) FROM articles a JOIN article_tags t ON CAST(a.id AS VARCHAR) = t.article_id WHERE t.tag = :tag AND a.status = :status",
           nativeQuery = true)
    Page<Article> findByTagAndStatus(@Param("tag") String tag, @Param("status") String status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE LOWER(a.categoryName) = LOWER(:categoryName) AND a.status = :status ORDER BY a.publishedAt DESC")
    Page<Article> findByCategoryNameAndStatus(@Param("categoryName") String categoryName, @Param("status") ArticleStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT t FROM Article a JOIN a.tags t ORDER BY t")
    List<String> findAllTags();
    
    @Query("SELECT t, COUNT(a) as count FROM Article a JOIN a.tags t WHERE a.status = :status GROUP BY t ORDER BY count DESC")
    List<Object[]> findTagsWithCount(@Param("status") ArticleStatus status);
    
    boolean existsByIdAndAuthorId(String id, String authorId);

    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query(value = "SELECT * FROM articles WHERE status = :status AND search_vector @@ plainto_tsquery('simple', :query) ORDER BY published_at DESC NULLS LAST",
           countQuery = "SELECT count(*) FROM articles WHERE status = :status AND search_vector @@ plainto_tsquery('simple', :query)",
           nativeQuery = true)
    Page<Article> searchByTsVector(@Param("query") String query, @Param("status") String status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = :status AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, @Param("status") ArticleStatus status, Pageable pageable);

    @Query("SELECT MAX(COALESCE(a.updatedAt, a.publishedAt, a.createdAt)) FROM Article a WHERE a.status = :status")
    LocalDateTime findLatestArticleTime(@Param("status") ArticleStatus status);
}