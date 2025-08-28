package com.pragma.controller;

import com.pragma.dto.UsuarioRequest;
import com.pragma.dto.UsuarioResponse;
import com.pragma.entities.User;
import com.pragma.usecase.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final RegistrarUsuarioUseCase useCase;

    @PostMapping
    public Mono<ResponseEntity<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest req) {
        log.info("[UsuarioController] POST /api/v1/usuarios email={}", req.getCorreoElectronico());

        User entity = User.builder()
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .fechaNacimiento(req.getFechaNacimiento())
                .direccion(req.getDireccion())
                .telefono(req.getTelefono())
                .correoElectronico(req.getCorreoElectronico())
                .salarioBase(req.getSalarioBase())
                .build();

        return null;/* useCase.ejecutar(entity)
                .map(saved -> {
                    UsuarioResponse r = new UsuarioResponse();
                    r.setId(saved.getId());
                    r.setNombres(saved.getNombres());
                    r.setApellidos(saved.getApellidos());
                    r.setFechaNacimiento(saved.getFechaNacimiento());
                    r.setDireccion(saved.getDireccion());
                    r.setTelefono(saved.getTelefono());
                    r.setCorreoElectronico(saved.getCorreoElectronico());
                    r.setSalarioBase(saved.getSalarioBase());
                    r.setRol(saved.getRol() != null ? saved.getRol().name() : null);
                    r.setEstado(saved.getEstado());
                    r.setFechaCreacion(saved.getFechaCreacion());

                    return ResponseEntity
                            .created(URI.create("/api/v1/usuarios/" + saved.getId()))
                            .body(r);
                });*/
    }
}
