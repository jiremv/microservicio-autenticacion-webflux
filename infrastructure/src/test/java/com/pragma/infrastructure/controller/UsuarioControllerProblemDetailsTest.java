package com.pragma.infrastructure.controller;

import com.pragma.domain.exception.EmailAlreadyExistsException;
import com.pragma.domain.model.Role;
import com.pragma.infrastructure.application.RegistrarUsuarioUseCase;
import com.pragma.infrastructure.persistence.entity.User;
import com.pragma.infrastructure.persistence.repository.UserRepository;
import com.pragma.infrastructure.web.dto.UsuarioRequest;
import com.pragma.infrastructure.web.dto.UsuarioResponse;
import com.pragma.infrastructure.web.handler.GlobalExceptionHandler;
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
@Import({ValidationAutoConfiguration.class, GlobalExceptionHandler.class})
class UsuarioControllerProblemDetailsTest {

    @Autowired WebTestClient client;
    @MockBean RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() { client = client.mutateWith(csrf()); }

    @Test
    void devuelve400_problemDetails_en_validacion() {
        // nombres vacío + correo inválido
        var body = """
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
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Solicitud inválida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                .jsonPath("$.errors").isArray();
        Mockito.verifyNoInteractions(useCase);
    }

    @Test
    void devuelve409_problemDetails_en_email_duplicado() {
        var body = """
        {
          "nombres": "Ana",
          "apellidos": "Pérez",
          "correoElectronico": "ana@demo.com",
          "salarioBase": 1000,
          "password": "secreto"
        }
        """;

        var existingId = UUID.randomUUID();
        Mockito.when(useCase.registrar(Mockito.any(UsuarioRequest.class)))
                .thenReturn(Mono.error(new EmailAlreadyExistsException("ana@demo.com","ana@demo.com")));

        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType("application/problem+json")
                .expectHeader().exists("X-Error-Id")
                .expectBody()
                .jsonPath("$.title").isEqualTo("Conflicto: recurso ya existe")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.code").isEqualTo("DUPLICATE_EMAIL")
                .jsonPath("$.errors[0].field").isEqualTo("correoElectronico");
                ///.jsonPath("$.conflictResource").isEqualTo("/api/v1/usuarios/" + existingId);
    }

    @Test
    void devuelve201_en_creacion_valida() {
        var body = """
        {
          "nombres": "Ana",
          "apellidos": "Pérez",
          "correoElectronico": "ana@demo.com",
          "salarioBase": 1000,
          "password": "secreto"
        }
        """;

        Mockito.when(useCase.registrar(Mockito.any(UsuarioRequest.class)))
                .thenReturn(Mono.just(UsuarioResponse.builder()
                        .id(UUID.randomUUID())
                        .build()));

        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated();
    }
}