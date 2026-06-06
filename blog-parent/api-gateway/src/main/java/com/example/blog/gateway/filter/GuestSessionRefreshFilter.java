package com.example.blog.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class GuestSessionRefreshFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public int getOrder() {
        return 3;
    }

    private static final String GUEST_SESSION_PREFIX = "guest:session:";
    private static final Duration GUEST_SESSION_TTL = Duration.ofHours(24);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if ("guest".equals(role) && userId != null) {
            String key = GUEST_SESSION_PREFIX + userId;
            return redisTemplate.expire(key, GUEST_SESSION_TTL)
                    .then(chain.filter(exchange));
        }

        return chain.filter(exchange);
    }
}
