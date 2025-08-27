package com.pragma.service.impl;

import com.pragma.service.JwtService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @Override
    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() -> extractClaim(token, Claims::getSubject))
                .onErrorResume(e -> Mono.empty());
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    @Override
    public Mono<Boolean> isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token)
                .map(username -> username.equals(userDetails.getUsername()) && !isTokenExpired(token))
                .onErrorReturn(false);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}

/*package com.pragma.service.impl;

import com.pragma.service.JwtService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor-
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    ///private String jwtSecret;

    private final String jwtSecret;
    private final Long jwtExpiration;

    @Override
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @Override
    public Mono<String> extractUserName(String token) {
        return Mono.fromCallable(() -> extractClaim(token, Claims::getSubject))
                .onErrorResume(e -> Mono.empty()); // Si falla, retorna vacío
    }

    @Override
    public Mono<Boolean> isTokenValid(String token, UserDetails userDetails) {
        return extractUserName(token)
                .map(username -> username.equals(userDetails.getUsername()) && !isTokenExpired(token))
                .onErrorReturn(false);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}*/