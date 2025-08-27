package com.pragma.service.impl;

import com.pragma.dao.request.SignUpRequest;
import com.pragma.dao.request.SigninRequest;
import com.pragma.dao.response.JwtAuthenticationResponse;
import com.pragma.entities.Role;
import com.pragma.entities.User;
import com.pragma.repository.UserRepository;
import com.pragma.service.AuthenticationService;
import com.pragma.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    public Mono<JwtAuthenticationResponse> signup(SignUpRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existing -> Mono.<JwtAuthenticationResponse>error(new RuntimeException("Usuario ya registrado con ese email")))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            User user = User.builder()
                                    .correoElectronico(request.getEmail())
                                    .password(passwordEncoder.encode(request.getPassword()))
                                    .nombres(request.getNombres())
                                    .apellidos(request.getApellidos())
                                    .rol(Role.USER)
                                    .build();

                            return userRepository.save(user)
                                .map(savedUser -> new JwtAuthenticationResponse(jwtService.generateToken(savedUser.getCorreoElectronico())));

                        })
                );
    }
    @Override
    public Mono<JwtAuthenticationResponse> signin(SigninRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new RuntimeException("Credenciales inválidas"));
                    }

                    String jwt = jwtService.generateToken(user.getCorreoElectronico());
                    return Mono.just(new JwtAuthenticationResponse(jwt));
                });
    }
}