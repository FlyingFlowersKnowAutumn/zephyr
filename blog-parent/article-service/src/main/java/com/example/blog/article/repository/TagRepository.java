package com.example.blog.article.repository;

import com.example.blog.article.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount + 1 WHERE t.id = :id")
    void incrementArticleCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount - 1 WHERE t.id = :id AND t.articleCount > 0")
    void decrementArticleCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE Tag t SET t.visitCount = t.visitCount + 1 WHERE t.id = :id")
    void incrementVisitCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE Tag t SET t.visitCount = t.visitCount + :delta WHERE t.id = :id")
    void incrementVisitCountByDelta(@Param("id") String id, @Param("delta") int delta);

    @Query("SELECT t FROM Tag t ORDER BY t.visitCount DESC")
    List<Tag> findAllOrderByVisitCountDesc();
}