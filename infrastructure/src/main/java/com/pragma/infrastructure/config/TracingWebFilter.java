package com.pragma.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TracingWebFilter implements WebFilter {
    private static final String TRACE_ID = "traceId";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        String finalTraceId = traceId;
        return chain.filter(exchange).contextWrite(ctx -> {
            MDC.put(TRACE_ID, finalTraceId);
            return ctx;
        });
    }
}
