package com.pragma.usecase;

import com.pragma.dto.UsuarioRequest;
import com.pragma.dto.UsuarioResponse;
import com.pragma.entities.Role;
import com.pragma.entities.User;
import com.pragma.domain.exception.EmailAlreadyExistsException;
import com.pragma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UserRepository userRepository;
    private final TransactionalOperator tx;
    private final PasswordEncoder passwordEncoder;

    // --- API principal: usada por el Controller ---
    public Mono<UsuarioResponse> registrar(UsuarioRequest req) {
        final String email = normalize(req.getCorreoElectronico());
        log.info("[RegistrarUsuario] inicio email={}", email);

        User nuevo = mapToEntity(req, email);
        aplicarDefaults(nuevo);

        // (Opcional pero consistente) Validar también aquí
        return validarNegocio(nuevo)
                .flatMap(this::pipelineGuardar)
                .map(this::mapToResponse)
                .as(tx::transactional);
    }

    // --- Bridge que tus tests llaman directamente ---
    @Deprecated
    public Mono<User> ejecutar(User nuevo) {
        final String email = normalize(nuevo.getCorreoElectronico());
        nuevo.setCorreoElectronico(email);
        log.info("[RegistrarUsuario] inicio email={}", email);

        aplicarDefaults(nuevo);

        // Los tests esperan IllegalArgumentException en inválidos
        return validarNegocio(nuevo)
                .flatMap(this::pipelineGuardar)
                .as(tx::transactional);
    }

    // --- Validaciones de negocio que esperan los tests ---
    private Mono<User> validarNegocio(User u) {
        if (u.getNombres() == null || u.getNombres().isBlank()
                || u.getApellidos() == null || u.getApellidos().isBlank()
                || u.getCorreoElectronico() == null || u.getCorreoElectronico().isBlank()) {
            return Mono.error(new IllegalArgumentException("Campos obligatorios vacíos"));
        }
        if (u.getSalarioBase() == null
                || u.getSalarioBase().compareTo(BigDecimal.ZERO) < 0
                || u.getSalarioBase().compareTo(new BigDecimal("15000000")) > 0) {
            return Mono.error(new IllegalArgumentException("salario_base fuera de rango"));
        }
        if (u.getPassword() == null || u.getPassword().isBlank()) {
            return Mono.error(new IllegalArgumentException("password es obligatorio"));
        }
        return Mono.just(u);
    }

    // --- Pipeline compartido ---
    private Mono<User> pipelineGuardar(User nuevo) {
        final String email = nuevo.getCorreoElectronico();

        return userRepository.findByCorreoElectronico(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("[RegistrarUsuario] duplicado email={}", email);
                        return Mono.error(new EmailAlreadyExistsException(
                                "El correo electrónico ya está registrado", "email"));
                    }
                    return Mono.fromCallable(() -> passwordEncoder.encode(nuevo.getPassword()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(hash -> { nuevo.setPassword(hash); return nuevo; })
                            .flatMap(userRepository::save)
                            .doOnSuccess(u -> {
                                if (u != null) {
                                    log.info("[RegistrarUsuario] OK id={}", u.getId());
                                } else {
                                    log.error("[RegistrarUsuario] save devolvió null");
                                }
                            });
                });
    }

    // --- Helpers ---
    private void aplicarDefaults(User u) {
        if (u.getRol() == null) u.setRol(Role.USER);
        if (u.getEstado() == null) u.setEstado(Boolean.TRUE);
        if (u.getFechaCreacion() == null) u.setFechaCreacion(LocalDateTime.now());
    }

    private String normalize(String s) {
        if (s == null) return null;
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private User mapToEntity(UsuarioRequest req, String emailNormalizado) {
        return User.builder()
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .fechaNacimiento(req.getFechaNacimiento())
                .direccion(req.getDireccion())
                .telefono(req.getTelefono())
                .correoElectronico(emailNormalizado)
                .salarioBase(req.getSalarioBase())
                .password(req.getPassword())
                .build();
    }

    private UsuarioResponse mapToResponse(User u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setNombres(u.getNombres());
        r.setApellidos(u.getApellidos());
        r.setFechaNacimiento(u.getFechaNacimiento());
        r.setDireccion(u.getDireccion());
        r.setTelefono(u.getTelefono());
        r.setCorreoElectronico(u.getCorreoElectronico());
        r.setSalarioBase(u.getSalarioBase());
        r.setRol(u.getRol().name());
        r.setEstado(u.getEstado());
        r.setFechaCreacion(u.getFechaCreacion());
        return r;
    }
}