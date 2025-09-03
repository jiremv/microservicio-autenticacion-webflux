package com.pragma.infrastructure.config;

import com.pragma.infrastructure.security.BearerTokenServerAuthenticationConverter;
import com.pragma.infrastructure.security.JwtAuthenticationManager;
import com.pragma.infrastructure.security.SelfOnlyAuthorizationManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final ReactiveUserDetailsService userDetailsService;
    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final BearerTokenServerAuthenticationConverter bearerConverter;
    private final SelfOnlyAuthorizationManager selfOnlyAuthorizationManager;

    public SecurityConfig(
            @Qualifier("myReactiveUserDetailsService") ReactiveUserDetailsService userDetailsService,
            JwtAuthenticationManager jwtAuthenticationManager,
            BearerTokenServerAuthenticationConverter bearerConverter,
            SelfOnlyAuthorizationManager selfOnlyAuthorizationManager
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.bearerConverter = bearerConverter;
        this.selfOnlyAuthorizationManager = selfOnlyAuthorizationManager;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Filtro JWT
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        jwtFilter.setServerAuthenticationConverter(bearerConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)   // 👈 deshabilita Basic
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)   // 👈 deshabilita formLogin
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios")
                        .hasAnyRole("ADMIN", "ASESOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/clientes/*/solicitudes-prestamo")
                        .access(selfOnlyAuthorizationManager) // CLIENTE + self-only
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}