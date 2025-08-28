package com.pragma.usecase;

import com.pragma.dto.UsuarioRequest;
import com.pragma.dto.UsuarioResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
class RegistrarUsuarioUseCaseMapperTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TransactionalOperator tx;
    @InjectMocks RegistrarUsuarioUseCase useCase;
    @BeforeEach
    void setup() {
        // Passthrough del operador transaccional para Mono/Flux
        lenient().when(tx.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(tx.transactional(any(Flux.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        // (No hace falta stubear execute(..) para estos tests)
    }

    private UsuarioRequest request() {
        UsuarioRequest r = new UsuarioRequest();
        r.setNombres("Ana");
        r.setApellidos("Pérez");
        r.setFechaNacimiento(LocalDate.of(1990,1,1));
        r.setDireccion("Calle 1");
        r.setTelefono("3001234567");
        r.setCorreoElectronico("ana.mapper@demo.com");
        r.setSalarioBase(new BigDecimal("1234.56"));
        r.setPassword("secreto");
        return r;
    }

    @Test
    void registrarRequestMapeaAResponseCorrectamente() {
        var req = request();

        when(userRepository.findByCorreoElectronico(req.getCorreoElectronico()))
                .thenReturn(Mono.empty());
        when(passwordEncoder.encode("secreto")).thenReturn("HASHED");

        var saved = User.builder()
                .id(UUID.randomUUID())
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .fechaNacimiento(req.getFechaNacimiento())
                .direccion(req.getDireccion())
                .telefono(req.getTelefono())
                .correoElectronico(req.getCorreoElectronico())
                .salarioBase(req.getSalarioBase())
                .password("HASHED")
                .rol(Role.USER)                 // default aplicado en registrar(User)
                .estado(true)                   // default aplicado en registrar(User)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        Mono<UsuarioResponse> mono = useCase.registrar(req);

        StepVerifier.create(mono)
                .assertNext(resp -> {
                    assertThat(resp.getId()).isEqualTo(saved.getId());
                    assertThat(resp.getCorreoElectronico()).isEqualTo(req.getCorreoElectronico());
                    assertThat(resp.getNombres()).isEqualTo(req.getNombres());
                    assertThat(resp.getApellidos()).isEqualTo(req.getApellidos());
                })
                .verifyComplete();

        // Captura y verifica entidad persistida
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        verify(passwordEncoder).encode("secreto");
        verify(userRepository).findByCorreoElectronico(req.getCorreoElectronico());
        assertThat(persisted.getPassword()).isEqualTo("HASHED");
        assertThat(persisted.getRol()).isEqualTo(Role.USER);
        assertThat(persisted.getEstado()).isTrue();
        assertThat(persisted.getFechaCreacion()).isNotNull();
    }

    @Test
    void registrarRequestConCorreoDuplicadoPropagaError() {
        var req = request();

        when(userRepository.findByCorreoElectronico(req.getCorreoElectronico()))
                .thenReturn(Mono.just(User.builder()
                        .id(UUID.randomUUID())
                        .correoElectronico(req.getCorreoElectronico())
                        .build()));

        StepVerifier.create(useCase.registrar(req))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).containsIgnoringCase("correo");
                })
                .verify();

        verify(userRepository, never()).save(any());
    }
}