package com.pragma.infrastructure.persistence.repository;

import com.pragma.infrastructure.persistence.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByCorreoElectronico(String correoElectronico);

}