package com.pragma.infrastructure.application;

import com.pragma.infrastructure.persistence.repository.UserRepository;
import com.pragma.infrastructure.security.JwtService;
import com.pragma.infrastructure.web.dto.LoginRequest;
import com.pragma.infrastructure.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutenticarUsuarioUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<LoginResponse> autenticar(LoginRequest req) {
        final String correoNorm = req.correo() == null ? "" : req.correo().trim().toLowerCase(Locale.ROOT);
        log.info("Intento de login para correo={}", correoNorm);

        return userRepository.findByCorreoElectronico(correoNorm)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Credenciales inválidas")))
                .flatMap(user -> {
                    boolean matches = passwordEncoder.matches(req.clave(), user.getPassword());
                    log.debug("Login diag: found={}, rol={}, estado={}, passLen={}, matches={}",
                            true,
                            user.getRol(), user.getEstado(),
                            user.getPassword() != null ? user.getPassword().length() : null,
                            matches);

                    if (!matches) {
                        return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
                    }
                    if (Boolean.FALSE.equals(user.getEstado())) {
                        return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
                    }
                    if (user.getId() == null || user.getRol() == null) {
                        log.error("Usuario inválido: id={} rol={}", user.getId(), user.getRol());
                        return Mono.error(new IllegalStateException("Usuario inválido"));
                    }

                    Map<String,Object> claims = new HashMap<>();
                    claims.put("uid", user.getId().toString());
                    claims.put("roles", List.of(user.getRol().name()));

                    String token = jwtService.generateToken(user.getCorreoElectronico(), claims);

                    return Mono.just(new LoginResponse(
                            token,
                            jwtService.getExpirationMillis(),
                            user.getId().toString(),
                            user.getCorreoElectronico(),
                            user.getRol().name()
                    ));
                });
    }
}
/*public class AutenticarUsuarioUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<LoginResponse> autenticar(LoginRequest req) {
        log.info("Intento de login para correo={}", req.correo());
        return userRepository.findByCorreoElectronico(req.correo())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Credenciales inválidas")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(req.clave(), user.getPassword())) {
                        return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
                    }
                    String token = jwtService.generateToken(
                            user.getCorreoElectronico(),
                            Map.of(
                                    "uid", user.getId().toString(),
                                    "roles", List.of(user.getRol().name())
                            )
                    );
                    log.info("Login exitoso para correo={}", user.getCorreoElectronico());
                    return Mono.just(new LoginResponse(
                            token,
                            jwtService.getExpirationMillis(),
                            user.getId().toString(),
                            user.getCorreoElectronico(),
                            user.getRol().name()
                    ));
                });
    }
}*/
