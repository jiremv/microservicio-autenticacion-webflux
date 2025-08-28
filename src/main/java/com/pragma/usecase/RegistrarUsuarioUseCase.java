package com.pragma.usecase;

/*import com.pragma.domain.exception.DuplicateEmailException;
import com.pragma.domain.port.UsuarioCommandPort;
import com.pragma.domain.port.UsuarioQueryPort;
import com.pragma.entities.Role;
import com.pragma.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor*/
public class RegistrarUsuarioUseCase {
/*
    private final UsuarioCommandPort commandPort;
    private final UsuarioQueryPort queryPort;
    private final TransactionalOperator tx;

    public Mono<User> ejecutar(User nuevo) {
        log.info("[RegistrarUsuario] inicio email={}", nuevo.getCorreoElectronico());

        return queryPort.existsByCorreo(nuevo.getCorreoElectronico())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("[RegistrarUsuario] duplicado email={}", nuevo.getCorreoElectronico());
                        return Mono.error(new DuplicateEmailException(nuevo.getCorreoElectronico()));
                    }
                    // defaults/normalización
                    if (nuevo.getId() == null) {
                        nuevo.setId(UUID.randomUUID());
                    }
                    if (nuevo.getRol() == null) {
                        nuevo.setRol(Role.USER); // ajusta a tu enum real
                    }
                    if (nuevo.getEstado() == null) {
                        nuevo.setEstado(Boolean.TRUE);
                    }
                    if (nuevo.getFechaCreacion() == null) {
                        nuevo.setFechaCreacion(LocalDateTime.now());
                    }
                    return commandPort.save(nuevo);
                })
                .doOnSuccess(u -> log.info("[RegistrarUsuario] OK id={}", u.getId()))
                .doOnError(e -> log.error("[RegistrarUsuario] error: {}", e.getMessage(), e))
                .as(tx::transactional);
    }*/
}
