package com.example.blog.common.dto;

import lombok.Data;

@Data
public class ArticleStatsUpdateDTO {
    private Integer commentCountDelta;
    private Integer likeCountDelta;
}
