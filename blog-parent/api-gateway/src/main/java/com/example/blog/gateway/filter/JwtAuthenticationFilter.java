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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关鉴权：
 * 1) 无条件剥离客户端可能伪造的身份头；
 * 2) 有 token 就解析，并用 token 内容重新注入身份头；
 * 3) 白名单只表示「允许匿名访问」，不再表示「跳过所有检查」。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** 客户端不可信的请求头，进入网关先一律删除 */
    private static final List<String> IDENTITY_HEADERS = List.of(
            "X-User-Id",
            "X-User-Role",
            "X-User-Name"
    );

    /**
     * 允许匿名访问的「方法 + 路径」规则。
     * 新增接口默认不公开（fail-safe），需要公开的必须显式登记。
     */
    private static final List<PublicRule> PUBLIC_RULES = List.of(
            // ===== 认证入口 =====
            new PublicRule("POST", "/api/v1/auth/login"),
            new PublicRule("POST", "/api/v1/auth/register"),
            new PublicRule("POST", "/api/v1/auth/guest-login"),
            new PublicRule("POST", "/api/v1/auth/send-code"),
            new PublicRule("PUT", "/api/v1/auth/password"),

            // ===== 文章：公开只读 =====
            new PublicRule("GET", "/api/v1/articles"),
            new PublicRule("GET", "/api/v1/articles/*"),
            new PublicRule("GET", "/api/v1/articles/slug/*"),
            new PublicRule("GET", "/api/v1/articles/author/*"),
            new PublicRule("GET", "/api/v1/articles/tag/*"),
            new PublicRule("GET", "/api/v1/articles/categories"),
            new PublicRule("GET", "/api/v1/articles/tags"),
            new PublicRule("GET", "/api/v1/articles/tags/count"),
            new PublicRule("GET", "/api/v1/articles/latest-time"),

            // ===== 标签 / 分类：公开只读 =====
            new PublicRule("GET", "/api/v1/tags"),
            new PublicRule("GET", "/api/v1/tags/hot"),
            new PublicRule("GET", "/api/v1/tags/*"),
            new PublicRule("POST", "/api/v1/tags/*/visit"),
            new PublicRule("GET", "/api/v1/categories"),
            new PublicRule("GET", "/api/v1/categories/*"),

            // ===== 评论 / 留言板：读公开，写需登录 =====
            new PublicRule("GET", "/api/v1/comments"),
            new PublicRule("GET", "/api/v1/comments/*/replies"),
            new PublicRule("GET", "/api/v1/comments/user/*"),
            new PublicRule("GET", "/api/v1/guestbook"),
            new PublicRule("GET", "/api/v1/guestbook/user/*"),

            // ===== 点赞 / 收藏的只读查询 =====
            // 前端详情页、个人主页在匿名状态下也会请求，且身份由网关从 token 重建、无法伪造，
            // 因此只读放开；POST/DELETE 仍然需要登录。
            new PublicRule("GET", "/api/v1/articles/*/reaction"),
            new PublicRule("GET", "/api/v1/articles/*/favorite"),
            new PublicRule("GET", "/api/v1/reactions/user/**"),
            new PublicRule("GET", "/api/v1/favorites/user/**"),

            // ===== 关于页 / 站点统计 =====
            new PublicRule("GET", "/api/v1/about"),
            new PublicRule("GET", "/api/v1/site/stats"),
            new PublicRule("POST", "/api/v1/site/view"),

            // ===== SEO =====
            new PublicRule("GET", "/api/v1/rss.xml"),
            new PublicRule("GET", "/api/v1/sitemap.xml"),

            // ===== 作者的公开资料（仅 profile 相关，不含管理接口） =====
            new PublicRule("GET", "/api/v1/users/*/profile"),
            new PublicRule("GET", "/api/v1/users/*/profile/info"),
            new PublicRule("GET", "/api/v1/users/*/announcement"),

            // ===== 静态上传资源 =====
            new PublicRule("GET", "/uploads/**")
    );

    private record PublicRule(String method, String pattern) {
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";

        // ① 剥离客户端伪造的身份头
        ServerHttpRequest sanitized = request.mutate()
                .headers(headers -> IDENTITY_HEADERS.forEach(headers::remove))
                .build();

        // ② 有 token 就解析，并用 token 内容重建身份头
        String token = extractToken(sanitized);
        Claims claims = token == null ? null : jwtService.parseToken(token);
        if (claims != null) {
            String userId = jwtService.getSubject(claims);
            String role = jwtService.getRole(claims);
            String finalRole = (role == null || role.isBlank()) ? "user" : role;
            sanitized = sanitized.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", finalRole)
                    .header("X-User-Name", userId)
                    .build();
        }

        // ③ 没有有效身份且不在白名单，拒绝
        if (claims == null && !isPublic(method, path)) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange.mutate().request(sanitized).build());
    }

    private boolean isPublic(String method, String path) {
        for (PublicRule rule : PUBLIC_RULES) {
            if (rule.method().equals(method) && MATCHER.match(rule.pattern(), path)) {
                return true;
            }
        }
        return false;
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
