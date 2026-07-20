package com.example.blog.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        log.info("Request [{}] {} {} from {}", 
                traceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getRemoteAddress());

        return chain.filter(exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-Trace-Id", traceId)
                                .build())
                        .build())
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;
                    log.info("Response [{}] status={}, duration={}ms", 
                            traceId, statusCode, duration);
                });
    }
}
