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
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UserRepository userRepository;
    private final TransactionalOperator tx;
    private final PasswordEncoder passwordEncoder;

    public Mono<UsuarioResponse> registrar(UsuarioRequest req) {
        log.info("[RegistrarUsuario] inicio email={}", req.getCorreoElectronico());

        // Validaciones de negocio mínimas (además de Bean Validation en el Controller)
        if (req.getNombres() == null || req.getNombres().isBlank()
                || req.getApellidos() == null || req.getApellidos().isBlank()
                || req.getCorreoElectronico() == null || req.getCorreoElectronico().isBlank()) {
            return Mono.error(new IllegalArgumentException("Campos obligatorios vacíos"));
        }
        if (req.getSalarioBase() == null
                || req.getSalarioBase().compareTo(BigDecimal.ZERO) < 0
                || req.getSalarioBase().compareTo(new BigDecimal("15000000")) > 0) {
            return Mono.error(new IllegalArgumentException("salario_base fuera de rango"));
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Mono.error(new IllegalArgumentException("password es obligatorio"));
        }

        // Normalizar email
        final String email = normalize(req.getCorreoElectronico());

        return userRepository.findByCorreoElectronico(email)
                .flatMap(existente -> {
                    log.warn("[RegistrarUsuario] duplicado email={}", email);
                    return Mono.error(new EmailAlreadyExistsException("El correo electrónico ya está registrado", "email"));
                })
                .switchIfEmpty(
                        Mono.defer(() ->
                                // hash en boundedElastic para no bloquear
                                Mono.fromCallable(() -> passwordEncoder.encode(req.getPassword()))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(hash -> User.builder()
                                                .nombres(req.getNombres())
                                                .apellidos(req.getApellidos())
                                                .fechaNacimiento(req.getFechaNacimiento())
                                                .direccion(req.getDireccion())
                                                .telefono(req.getTelefono())
                                                .correoElectronico(email)
                                                .salarioBase(req.getSalarioBase())
                                                .password(hash)
                                                .rol(Role.USER)
                                                .estado(Boolean.TRUE)
                                                .fechaCreacion(LocalDateTime.now())
                                                .build()
                                        )
                                        .flatMap(userRepository::save)
                                        .doOnSuccess(u -> log.info("[RegistrarUsuario] OK id={}", u.getId()))
                        )
                )
                .cast(User.class)
                .map(this::toResponse)
                .as(tx::transactional);
    }
    private UsuarioResponse toResponse(User u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .fechaNacimiento(u.getFechaNacimiento())
                .direccion(u.getDireccion())
                .telefono(u.getTelefono())
                .correoElectronico(u.getCorreoElectronico())
                .salarioBase(u.getSalarioBase())
                .rol(u.getRol())
                .estado(u.getEstado())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }
    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}