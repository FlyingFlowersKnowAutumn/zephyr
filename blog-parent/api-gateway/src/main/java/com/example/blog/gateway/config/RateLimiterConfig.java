package com.example.blog.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * 限流的 key：优先取 nginx 透传的真实客户端 IP。
     * 网关是唯一入口且只监听 127.0.0.1，因此信任 X-Forwarded-For 的第一跳是安全的。
     * 如果以后把网关直接暴露公网，需要改成在 nginx 层限流。
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String clientIp = forwarded.split(",")[0].trim();
                if (!clientIp.isEmpty()) {
                    return Mono.just(clientIp);
                }
            }
            String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return Mono.just(realIp.trim());
            }
            return Mono.just(exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown");
        };
    }
}
