package com.pragma.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import com.pragma.entities.User;
import reactor.core.publisher.Mono;
public interface UserService extends ReactiveUserDetailsService {
    Mono<User> findByEmail(String email);
}