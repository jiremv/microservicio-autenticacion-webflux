package com.pragma.controller;

import com.pragma.dto.UsuarioRequest;
import com.pragma.dto.UsuarioResponse;
import com.pragma.usecase.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.net.URI;
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final RegistrarUsuarioUseCase useCase;
    /*@PostMapping
    public Mono<ResponseEntity<Void>> crear(@Valid @RequestBody UsuarioRequest req) {
        return useCase.registrar(req)
                .map(r -> ResponseEntity.created(URI.create("/api/v1/usuarios/" + r.getId())).body(r).build());
    }*/
    @PostMapping
    public Mono<ResponseEntity<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest req) {
        return useCase.registrar(req)
                .map(r -> ResponseEntity
                        .created(URI.create("/api/v1/usuarios/" + r.getId()))
                        .body(r)
                );
    }

}
