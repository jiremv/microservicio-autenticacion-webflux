package com.pragma.entities;

///import jakarta.persistence.EnumType;
///import jakarta.persistence.Enumerated;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("usuarios") // nombre de la tabla en PostgreSQL
public class User {

    @Id
    private UUID id;

    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    //@Column(name = "correo_electronico", unique = true)
    @Column("correo_electronico")
    private String correoElectronico;
    private Double salarioBase;
    private String password;
    //@Enumerated(EnumType.STRING)
    @Column("rol")
    private Role rol;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
}
