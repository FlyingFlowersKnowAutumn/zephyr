package com.example.blog.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 把网关透传的 X-Trace-Id 写进 MDC，让 Result.traceId 和日志都能带上链路 ID。
 * 仅用于 servlet 栈服务（article / user / interaction）；网关是 WebFlux，不扫描该包。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = null;
        if (request instanceof HttpServletRequest httpRequest) {
            traceId = httpRequest.getHeader(TRACE_HEADER);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_MDC_KEY, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            // 线程池复用线程，必须清理，否则下一个请求会继承上一个的 traceId
            MDC.remove(TRACE_MDC_KEY);
        }
    }
}
