package com.pragma.config;

import com.pragma.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;

//@RequiredArgsConstructor
public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationWebFilter(JwtService jwtService, ReactiveUserDetailsService userDetailsService) {
        super(authenticationManager(jwtService, userDetailsService));

        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
        setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                    String token = authHeader.substring(BEARER_PREFIX.length());
                    return jwtService.extractUsername(token)
                            .flatMap(username ->
                                    userDetailsService.findByUsername(username)
                                            .flatMap(userDetails ->
                                                    jwtService.isTokenValid(token, userDetails)
                                                            .filter(Boolean::booleanValue)
                                                            .map(valid -> new UsernamePasswordAuthenticationToken(
                                                                    userDetails, null, userDetails.getAuthorities()))
                                            )
                            );
                }
                return Mono.empty();
            }
        });
        setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
    }

    // Este AuthenticationManager se usa solo para validar el userDetails que ya cargamos desde el token
    private static ReactiveAuthenticationManager authenticationManager(
            JwtService jwtService,
            ReactiveUserDetailsService userDetailsService) {
        return authentication -> Mono.just(authentication); // ya autenticado arriba
    }
}