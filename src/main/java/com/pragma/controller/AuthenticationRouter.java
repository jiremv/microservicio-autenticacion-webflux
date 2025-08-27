package com.pragma.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
@Configuration
public class AuthenticationRouter {
    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthenticationHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/auth/signup", handler::signup)
                .POST("/api/v1/auth/signin", handler::signin)
                .build();
    }
}
