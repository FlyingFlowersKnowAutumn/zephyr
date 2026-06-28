package com.example.blog.user.repository;

import com.example.blog.user.entity.SiteStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteStatsRepository extends JpaRepository<SiteStats, Long> {

    @Modifying
    @Query("UPDATE SiteStats s SET s.viewCount = s.viewCount + 1 WHERE s.id = 1")
    int incrementViewCount();

    @Modifying
    @Query("UPDATE SiteStats s SET s.visitorCount = s.visitorCount + 1 WHERE s.id = 1")
    int incrementVisitorCount();
}
