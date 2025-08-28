package com.pragma.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UsuarioResponse {
    private UUID id;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private Double salarioBase;
    private String rol;              // texto conveniente para el cliente
    private Boolean estado;
    private LocalDateTime fechaCreacion;
}
