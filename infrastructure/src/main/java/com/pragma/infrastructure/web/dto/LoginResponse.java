package com.pragma.infrastructure.web.dto;

public record LoginResponse(
        String token,
        long expiresInMillis,
        String userId,
        String correo,
        String rol
) {}
