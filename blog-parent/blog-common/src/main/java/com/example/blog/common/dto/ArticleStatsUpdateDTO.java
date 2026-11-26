package com.example.blog.common.dto;

import lombok.Data;

/**
 * 文章互动计数的同步载荷。
 *
 * <p>注意：这里传的是 <b>绝对值（覆盖语义）</b>，不是增量。
 *
 * <p>早期用的是增量（commentCountDelta / likeCountDelta），问题在于：一次 HTTP
 * 超时或 article-service 重启，这次增量就永久丢了，而调用方只打了条 warn 日志，
 * 没有任何补偿 —— 计数会越飘越远且无法自愈。
 *
 * <p>改成绝对值后，每次操作都把明细表算出来的真实总数推过去：
 * <ul>
 *   <li>同步天然幂等，重试、乱序都安全；</li>
 *   <li>任何一次失败都会被下一次操作自动纠正回来，不需要人工对账。</li>
 * </ul>
 *
 * <p>字段为 null 表示本次不改该项。
 */
@Data
public class ArticleStatsUpdateDTO {
    private Integer commentCount;
    private Integer likeCount;
}
