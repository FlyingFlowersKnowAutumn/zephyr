package com.example.blog.gateway.filter;

import com.example.blog.gateway.config.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Override
    public int getOrder() {
        return 2;
    }

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/guest-login",
            "/api/v1/auth/send-code",
            "/api/v1/auth/password",
            "/api/v1/about",
            "/api/v1/site/view"
    );

    private static final Set<String> READ_ONLY_WHITELIST = Set.of(
            "/api/v1/articles",
            "/api/v1/tags",
            "/api/v1/categories",
            "/api/v1/site",
            "/api/v1/about",
            "/api/v1/rss",
            "/api/v1/sitemap",
            "/api/v1/guestbook",
            "/api/v1/users",
            "/api/v1/reactions",
            "/api/v1/favorites",
            "/uploads"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";

        if (isExactWhitelisted(path)) {
            return chain.filter(exchange);
        }

        if ("GET".equals(method) && isReadOnlyWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null) {
            return unauthorized(exchange);
        }

        Claims claims = jwtService.parseToken(token);
        if (claims == null) {
            return unauthorized(exchange);
        }

        String userId = jwtService.getSubject(claims);
        String role = jwtService.getRole(claims);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .header("X-User-Name", userId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isExactWhitelisted(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private boolean isReadOnlyWhitelisted(String path) {
        return READ_ONLY_WHITELIST.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
