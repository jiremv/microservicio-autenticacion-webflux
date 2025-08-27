package com.pragma.service;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class JwtAuthenticationFilter extends AuthenticationWebFilter {
    private final JwtService jwtService;
    private final ReactiveUserDetailsService uds;
    public JwtAuthenticationFilter(JwtService jwtService, ReactiveUserDetailsService uds) {
        super((Authentication authentication) -> Mono.empty()); // stateless
        this.jwtService = jwtService;
        this.uds = uds;

        setServerAuthenticationConverter(this::convert);
    }
    private Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty();
        }

        String token = authHeader.substring(7);
        return jwtService.extractUsername(token)
                .flatMap(username -> uds.findByUsername(username)
                        .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        )));
    }
}