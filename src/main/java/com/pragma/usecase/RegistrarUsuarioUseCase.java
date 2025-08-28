package com.pragma.usecase;

import com.pragma.entities.Role;
import com.pragma.entities.User;
import com.pragma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UserRepository userRepository;
    private final TransactionalOperator tx;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> ejecutar(User nuevo) {
        log.info("[RegistrarUsuario] inicio email={}", nuevo.getCorreoElectronico());

        // Validaciones mínimas (además de las @Valid en el DTO)
        if (nuevo.getNombres() == null || nuevo.getNombres().isBlank()
                || nuevo.getApellidos() == null || nuevo.getApellidos().isBlank()
                || nuevo.getCorreoElectronico() == null || nuevo.getCorreoElectronico().isBlank()) {
            return Mono.error(new IllegalArgumentException("Campos obligatorios vacíos"));
        }
        if (nuevo.getSalarioBase() == null
                || nuevo.getSalarioBase().compareTo(BigDecimal.ZERO) < 0
                || nuevo.getSalarioBase().compareTo(new BigDecimal("15000000")) > 0) {
            return Mono.error(new IllegalArgumentException("salario_base fuera de rango"));
        }
        if (nuevo.getPassword() == null || nuevo.getPassword().isBlank()) {
            return Mono.error(new IllegalArgumentException("password es obligatorio"));
        }

        return userRepository.findByCorreoElectronico(nuevo.getCorreoElectronico())
                .flatMap(existing -> {
                    log.warn("[RegistrarUsuario] duplicado email={}", nuevo.getCorreoElectronico());
                    return Mono.<User>error(new IllegalArgumentException("El correo electrónico ya está registrado"));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Deja que el DB genere el id/rol/estado/fecha_creacion vía DEFAULT,
                    // o si prefieres fijarlos en Java, descomenta:
                    // if (nuevo.getId() == null) nuevo.setId(UUID.randomUUID());
                    if (nuevo.getRol() == null) nuevo.setRol(Role.USER);
                    if (nuevo.getEstado() == null) nuevo.setEstado(Boolean.TRUE);
                    if (nuevo.getFechaCreacion() == null) nuevo.setFechaCreacion(LocalDateTime.now());

                    // Hash de password
                    nuevo.setPassword(passwordEncoder.encode(nuevo.getPassword()));

                    return userRepository.save(nuevo)
                            .doOnSuccess(u -> log.info("[RegistrarUsuario] OK id={}", u.getId()))
                            .doOnError(e -> log.error("[RegistrarUsuario] error: {}", e.getMessage(), e));
                }))
                .as(tx::transactional);
    }
}
