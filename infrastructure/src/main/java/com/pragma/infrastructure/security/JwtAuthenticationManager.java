package com.pragma.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        try {
            Jws<Claims> jws = jwtService.parse(token);
            Claims claims = jws.getPayload();
            String subject = claims.getSubject();
            Object rolesObj = claims.get("roles");
            List<String> roles = rolesObj instanceof List<?> l
                    ? l.stream().map(Object::toString).collect(Collectors.toList())
                    : List.of();
            List<GrantedAuthority> authorities = roles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            JwtAuthenticationToken authToken =
                    new JwtAuthenticationToken(subject, token, authorities, claims);
            authToken.setDetails(claims);
            return Mono.just(authToken);
        } catch (JwtException e) {
            return Mono.error(new BadCredentialsException("Invalid JWT", e));
        }
    }

    public static class JwtAuthenticationToken extends AbstractAuthenticationToken {
        private final String principal;
        private final String token;
        private final Claims claims;

        public JwtAuthenticationToken(String principal, String token,
                                      List<GrantedAuthority> authorities, Claims claims) {
            super(authorities);
            this.principal = principal;
            this.token = token;
            this.claims = claims;
            setAuthenticated(true);
            setDetails(claims);
        }
        @Override public Object getCredentials() { return token; }
        @Override public Object getPrincipal() { return principal; }
        public Claims getClaims() { return claims; }
    }
}
