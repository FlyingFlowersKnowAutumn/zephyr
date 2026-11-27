package com.example.blog.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * 限流的 key：取 nginx 透传的真实客户端 IP。
     *
     * <p>安全前提：nginx 端用 {@code proxy_set_header X-Forwarded-For $client_ip} <b>覆盖</b>
     * 了这个头（值是 Cloudflare 的 CF-Connecting-IP 或 TCP 对端地址），客户端自己伪造的值
     * 在那一跳就被丢掉了。这里按第一段取是安全的——只要那段覆盖逻辑还在。
     *
     * <p>如果哪天改回 {@code $proxy_add_x_forwarded_for}（追加写法），客户端就能靠每次换一个
     * 伪造头绕过限流：实测过，250 次请求全部返回 200，一条 429 都没触发。
     *
     * <p>网关只监听 127.0.0.1，请求必然来自 nginx；如果把网关直接暴露公网，
     * 就必须改成在 nginx 层限流，否则请求可以不带这个头直接打网关。
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
