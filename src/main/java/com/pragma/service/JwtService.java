package com.pragma.service;

import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface JwtService {
    Mono<String> extractUsername(String token);

    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver);

    String generateToken(String username);

    Mono<Boolean> isTokenValid(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);
}