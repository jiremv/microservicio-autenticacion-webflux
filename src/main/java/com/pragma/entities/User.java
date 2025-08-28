package com.pragma.entities;

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
    private String nombres;
    private String apellidos;
    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    @Column("correo_electronico")
    private String correoElectronico;
    @Column("salario_base")
    private BigDecimal salarioBase; // <-- NUMERIC(10,2)
    private String password; // columna es 'password', ya coincide
    @Column("rol")
    private Role rol;
    private Boolean estado;
    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;
}
