package com.pragma.service.impl;

import com.pragma.repository.UserRepository;
import com.pragma.entities.User;
import com.pragma.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;
    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmail(username)
                .map(usuario -> org.springframework.security.core.userdetails.User
                        .withUsername(usuario.getCorreoElectronico())
                        .password(usuario.getPassword())
                        .roles("USER") // o usar authorities()
                        .build()
                );
    }

}