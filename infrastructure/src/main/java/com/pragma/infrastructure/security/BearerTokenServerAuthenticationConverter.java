package com.pragma.infrastructure.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;                    // <-- importa Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {   // <-- Mono<Authentication>
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            return Mono.just(new UsernamePasswordAuthenticationToken("N/A", token));
        }
        return Mono.empty();
    }
}
