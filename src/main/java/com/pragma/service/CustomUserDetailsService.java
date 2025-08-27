package com.pragma.service;
/*
import com.pragma.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service*/
public class CustomUserDetailsService{/*} implements ReactiveUserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByCorreoElectronico(username).cast(UserDetails.class);
    }*/
}
