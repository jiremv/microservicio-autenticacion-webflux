package com.pragma.infrastructure.client;

import com.pragma.infrastructure.persistence.repository.UserRepository; // Repositorio propio ✔️
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService; // Interfaz Spring Security ✔️
import org.springframework.security.core.userdetails.UserDetails; // Tipo de retorno ✔️
import org.springframework.stereotype.Service; // Anotación para el bean ✔️
import reactor.core.publisher.Mono; // Para programación reactiva ✔️
@Service("myReactiveUserDetailsService")
@Primary
public class MyReactiveUserDetailsService implements ReactiveUserDetailsService {
    private final UserRepository userRepository;
    public MyReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByCorreoElectronico(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getCorreoElectronico())
                        .password(user.getPassword())
                        .authorities(user.getRol().name()) // necesito mapear el rol a String
                        ///.roles(user.getRol().name()) // rol debe ser una cadena como 'ADMIN' o 'USER'
                        .build());
    }
}
