package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.common.dto.SiteStatsDTO;
import com.example.blog.user.service.SiteStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/site")
@RequiredArgsConstructor
public class SiteStatsController {

    private final SiteStatsService siteStatsService;

    @GetMapping("/stats")
    public Result<SiteStatsDTO> getSiteStats() {
        return Result.success(siteStatsService.getSiteStats());
    }

    @PostMapping("/view")
    public Result<Void> incrementViewCount() {
        siteStatsService.incrementViewCount();
        return Result.success(null);
    }
}
