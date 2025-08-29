package com.pragma.usecase;

import com.pragma.entities.Role;
import com.pragma.entities.User;
import com.pragma.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {
    @Mock
    UserRepository userRepository;
    @Mock
    TransactionalOperator tx;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        // Passthrough transaccional para Mono/Flux
        lenient().when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(tx.transactional(any(Flux.class))).thenAnswer(inv -> inv.getArgument(0));
        // Reinyecta por si @InjectMocks no alcanzó
        useCase = new RegistrarUsuarioUseCase(userRepository, tx, passwordEncoder);
        //evita NPE en tests que no stubean findBy... explícitamente
        lenient().when(userRepository.findByCorreoElectronico(any())).thenReturn(Mono.empty());
    }

    private User baseUser() {
        return User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                // .tipoDocumento("CC")
                // .numeroDocumento("123456789")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .direccion("Calle 1")
                .telefono("3001234567")
                .correoElectronico("ana.perez@demo.com")
                .salarioBase(new BigDecimal("2500.50"))
                .password("secreto")
                .build();
    }
    @Test
    void fallaCuandoCamposObligatoriosVacios() {
        User invalido = User.builder()
                .nombres("")                           // vacío
                .apellidos(" ")
                .correoElectronico(null)               // null
                .salarioBase(new BigDecimal("1000"))
                .password("x")
                .build();

        StepVerifier.create(useCase.ejecutar(invalido))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).containsIgnoringCase("obligatorios");
                })
                .verify();

        verifyNoInteractions(userRepository);
    }
    @Test
    void fallaCuandoSalarioFueraDeRango_negativo() {
        User invalido = User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@demo.com")
                .salarioBase(new BigDecimal("-1"))
                .password("demo")
                .build();

        StepVerifier.create(useCase.ejecutar(invalido))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).containsIgnoringCase("salario_base");
                })
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void fallaCuandoSalarioFueraDeRango_mayorQueMaximo() {
        User invalido = User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@demo.com")
                .salarioBase(new BigDecimal("15000000.01"))
                .password("demo")
                .build();

        StepVerifier.create(useCase.ejecutar(invalido))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).containsIgnoringCase("salario_base");
                })
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void fallaCuandoPasswordObligatoria() {
        User invalido = User.builder()
                .nombres("Ana")
                .apellidos("Pérez")
                .correoElectronico("ana@demo.com")
                .salarioBase(new BigDecimal("1000"))
                .password(" ") // en blanco
                .build();

        StepVerifier.create(useCase.ejecutar(invalido))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).containsIgnoringCase("password");
                })
                .verify();

        verifyNoInteractions(userRepository);
    }
}
