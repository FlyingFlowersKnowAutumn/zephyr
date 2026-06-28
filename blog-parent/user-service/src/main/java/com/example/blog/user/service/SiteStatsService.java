package com.example.blog.user.service;

import com.example.blog.common.dto.SiteStatsDTO;
import com.example.blog.user.entity.SiteStats;
import com.example.blog.user.repository.SiteStatsRepository;
import com.example.blog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteStatsService {

    private final SiteStatsRepository siteStatsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SiteStatsDTO getSiteStats() {
        SiteStats stats = siteStatsRepository.findById(1L).orElseGet(() ->
                SiteStats.builder().visitorCount(0L).viewCount(0L).articleCount(0L).build()
        );

        long userCount = userRepository.count();
        stats.setVisitorCount(userCount);

        return convertToDTO(stats);
    }

    @Transactional
    public void incrementViewCount() {
        siteStatsRepository.incrementViewCount();
    }

    private SiteStatsDTO convertToDTO(SiteStats stats) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return SiteStatsDTO.builder()
                .visitorCount(stats.getVisitorCount())
                .viewCount(stats.getViewCount())
                .articleCount(stats.getArticleCount())
                .lastUpdate(stats.getLastUpdate() != null ? stats.getLastUpdate().format(formatter) : null)
                .build();
    }
}
