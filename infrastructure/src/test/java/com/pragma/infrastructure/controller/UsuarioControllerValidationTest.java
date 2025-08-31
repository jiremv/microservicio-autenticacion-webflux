package com.pragma.infrastructure.controller;

import com.pragma.infrastructure.application.RegistrarUsuarioUseCase;
import com.pragma.infrastructure.web.dto.UsuarioRequest;
import com.pragma.infrastructure.web.dto.UsuarioResponse;
import com.pragma.infrastructure.persistence.entity.User;
import com.pragma.infrastructure.persistence.repository.UserRepository;
import com.pragma.infrastructure.web.controller.UsuarioController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.UUID;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(
        controllers = UsuarioController.class,
        excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class
)
@Import(ValidationAutoConfiguration.class)
class UsuarioControllerValidationTest {
    @Autowired
    WebTestClient client;
    @MockBean
    RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        // agrega CSRF a TODAS las requests del client
        client = client.mutateWith(csrf());
    }

    @Test
    void rechazaPayloadInvalido_y_no_llama_useCase() {
        // nombres vacío + correo con coma
        String body = """
        {
          "nombres": "",
          "apellidos": "x",
          "correoElectronico": "j@c,com",
          "salarioBase": 1000,
          "password": "x"
        }
        """;

        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(useCase);
    }

    @Test
    void aceptaPayloadValido_y_devuelve201() {
        String body = """
        {
          "nombres": "Ana",
          "apellidos": "Pérez",
          "correoElectronico": "ana@demo.com",
          "salarioBase": 1000,
          "password": "secreto"
        }
        """;
        // IMPORTANTE: que el stub devuelva un id para construir el Location
        Mockito.when(useCase.registrar(Mockito.any(UsuarioRequest.class)))
                .thenReturn(Mono.just(UsuarioResponse.builder()
                        .id(UUID.randomUUID())
                        .build()));

        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated();

        Mockito.verify(useCase).registrar(Mockito.any(UsuarioRequest.class));
    }
}