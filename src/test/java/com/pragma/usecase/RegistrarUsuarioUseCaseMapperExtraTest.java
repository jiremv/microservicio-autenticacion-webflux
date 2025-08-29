package com.pragma.usecase;

import com.pragma.domain.exception.EmailAlreadyExistsException;
import com.pragma.dto.UsuarioRequest;
import com.pragma.dto.UsuarioResponse;
import com.pragma.entities.Role;
import com.pragma.entities.User;
import com.pragma.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseMapperExtraTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TransactionalOperator tx;

    @InjectMocks RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setup() {
        lenient().when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(tx.transactional(any(Flux.class))).thenAnswer(inv -> inv.getArgument(0));
        // Evita NPE cuando no se stubee explícitamente en un test:
        lenient().when(userRepository.findByCorreoElectronico(any())).thenReturn(Mono.empty());
    }

    private UsuarioRequest req(String email) {
        var r = new UsuarioRequest();
        r.setNombres("Ana");
        r.setApellidos("Pérez");
        r.setFechaNacimiento(LocalDate.of(1990,1,1));
        r.setDireccion("Calle 1");
        r.setTelefono("3001234567");
        r.setCorreoElectronico(email);
        r.setSalarioBase(new BigDecimal("500.00"));
        r.setPassword("secreto");
        return r;
    }

    @Test
    void mapeaCamposBasicosYDefaults() {
        var req = req("mapper1@demo.com");
        when(userRepository.findByCorreoElectronico(req.getCorreoElectronico())).thenReturn(Mono.empty());
        when(passwordEncoder.encode("secreto")).thenReturn("HASH");

        var saved = User.builder()
                .id(UUID.randomUUID())
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .fechaNacimiento(req.getFechaNacimiento())
                .direccion(req.getDireccion())
                .telefono(req.getTelefono())
                .correoElectronico(req.getCorreoElectronico())
                .salarioBase(req.getSalarioBase())
                .password("HASH")
                .rol(Role.USER)
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        Mono<UsuarioResponse> mono = useCase.registrar(req);

        StepVerifier.create(mono)
                .assertNext(r -> {
                    assertThat(r.getId()).isEqualTo(saved.getId());
                    assertThat(r.getCorreoElectronico()).isEqualTo(req.getCorreoElectronico());
                    assertThat(r.getNombres()).isEqualTo(req.getNombres());
                    assertThat(r.getApellidos()).isEqualTo(req.getApellidos());
                })
                .verifyComplete();

        // Verificar que se pasó el password encriptado al repo
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        assertThat(cap.getValue().getPassword()).isEqualTo("HASH");
        verify(passwordEncoder).encode("secreto");
    }

    @Test
    void propagacionDeErrorPorCorreoDuplicadoDesdeMapper() {
        var req = req("dup@demo.com");
        when(userRepository.findByCorreoElectronico(req.getCorreoElectronico()))
                .thenReturn(Mono.just(User.builder().id(UUID.randomUUID()).correoElectronico(req.getCorreoElectronico()).build()));

        StepVerifier.create(useCase.registrar(req))
                .expectErrorSatisfies(ex -> {
                assertThat(ex).isInstanceOf(EmailAlreadyExistsException.class);
                // Evita matchear mensaje exacto (acentos en consola). Si quieres:
                    // assertThat(ex.getMessage().toLowerCase()).contains("registrado");
                })
                .verify();

        verify(userRepository, never()).save(any());
    }
}