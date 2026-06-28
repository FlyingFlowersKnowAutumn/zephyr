package com.example.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteStatsDTO {

    private Long visitorCount;

    private Long viewCount;

    private Long articleCount;

    private String lastUpdate;
}
