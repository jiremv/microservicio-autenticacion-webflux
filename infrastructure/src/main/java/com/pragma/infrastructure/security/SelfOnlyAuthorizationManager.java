package com.pragma.infrastructure.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SelfOnlyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        String path = context.getExchange().getRequest().getPath().value();
        String[] parts = path.split("/");
        String clienteId = null;
        for (int i=0;i<parts.length;i++) {
            if ("clientes".equals(parts[i]) && i+1 < parts.length) {
                clienteId = parts[i+1];
                break;
            }
        }
        String finalClienteId = clienteId;
        return authentication.map(auth -> {
            boolean hasRoleClient = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
            String uid = auth.getDetails() instanceof io.jsonwebtoken.Claims c
                    ? c.get("uid", String.class) : null;
            boolean decision = hasRoleClient && finalClienteId != null && uid != null && uid.equals(finalClienteId);
            return new AuthorizationDecision(decision);
        }).defaultIfEmpty(new AuthorizationDecision(false));
    }
}
