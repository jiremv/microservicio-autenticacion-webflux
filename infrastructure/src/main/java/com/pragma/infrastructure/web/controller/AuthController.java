package com.pragma.infrastructure.web.controller;

import com.pragma.infrastructure.application.AutenticarUsuarioUseCase;
import com.pragma.infrastructure.web.dto.LoginRequest;
import com.pragma.infrastructure.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticarUsuarioUseCase useCase;

    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        return useCase.autenticar(req).map(ResponseEntity::ok);
    }
}
