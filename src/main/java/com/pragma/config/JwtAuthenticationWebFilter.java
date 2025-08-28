package com.pragma.config;

import com.pragma.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    // Define aquí las rutas públicas para que el filtro las ignore
    private static final Set<String> PUBLIC_POST = Set.of(
            "/api/v1/usuarios",
            "/auth/login",
            "/auth/register"
    );

    public JwtAuthenticationWebFilter(JwtService jwtService, ReactiveUserDetailsService userDetailsService) {
        super(authenticationManager());

        // ⚠️ Cambiamos el requiresAuthenticationMatcher:
        // Solo intenta autenticar si hay header Authorization: Bearer ... Y NO es ruta pública.
        setRequiresAuthenticationMatcher((ServerWebExchange exchange) -> {
            ServerHttpRequest req = exchange.getRequest();
            String path = req.getURI().getPath();

            // preflight nunca debe intentar autenticarse
            if (req.getMethod() == HttpMethod.OPTIONS) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }

            // rutas públicas (coincidir con SecurityConfig)
            boolean isPublic = (req.getMethod() == HttpMethod.POST && PUBLIC_POST.contains(path))
                    || path.startsWith("/swagger-ui/")
                    || path.startsWith("/v3/api-docs/")
                    || path.equals("/actuator/health")
                    || path.equals("/actuator/info");

            if (isPublic) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }

            // Solo match si hay Bearer
            String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            boolean hasBearer = auth != null && auth.startsWith(BEARER_PREFIX);
            return hasBearer
                    ? ServerWebExchangeMatcher.MatchResult.match()
                    : ServerWebExchangeMatcher.MatchResult.notMatch();
        });

        // Convertidor: si hay Bearer, valida; si no, nunca llega aquí porque el matcher no matchea
        setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                    return Mono.empty();
                }
                String token = authHeader.substring(BEARER_PREFIX.length());
                // extractUsername -> Mono<String>, isTokenValid -> Mono<Boolean>
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
        });

        // No persistas el contexto entre requests
        setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
    }

    // Ya no validamos credenciales aquí: el token “ya valida” en el converter
    private static ReactiveAuthenticationManager authenticationManager() {
        return authentication -> Mono.just(authentication);
    }
}
