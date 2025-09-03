package com.pragma.infrastructure.persistence.entity;

import com.pragma.domain.model.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("usuarios")
public class User {

    @Id
    private UUID id;

    @Column("nombres")
    private String nombres;

    @Column("apellidos")
    private String apellidos;

    @Column("documento_identidad")
    private String documentoIdentidad;

    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column("direccion")
    private String direccion;

    @Column("telefono")
    private String telefono;

    @Column("correo_electronico")  // <-- IMPORTANTE: snake_case
    private String correoElectronico;

    @Column("salario_base")
    private BigDecimal salarioBase;

    @Column("password")
    private String password;        // BCrypt ($2a$...)

    @Column("rol")
    private Role rol;               // ADMIN/ASESOR/CLIENTE

    @Column("estado")
    private Boolean estado;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;
}
