package com.pragma.infrastructure.persistence.repository;

import com.pragma.infrastructure.persistence.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import org.springframework.data.r2dbc.repository.Query;
import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    @Query("SELECT * FROM usuarios WHERE LOWER(correo_electronico) = LOWER($1)")
    Mono<User> findByCorreoElectronico(String correo);
}

/*
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByCorreoElectronico(String correoElectronico);

}*/