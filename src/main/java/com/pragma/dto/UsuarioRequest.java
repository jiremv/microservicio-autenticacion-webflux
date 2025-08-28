package com.pragma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UsuarioRequest {
    @NotBlank(message = "nombres es obligatorio")
    private String nombres;

    @NotBlank(message = "apellidos es obligatorio")
    private String apellidos;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento; // opcional

    private String direccion; // opcional
    private String telefono;  // opcional

    @NotBlank(message = "correoElectronico es obligatorio")
    @Email(message = "correoElectronico no tiene formato válido")
    private String correoElectronico;

    @NotNull(message = "salarioBase es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "salarioBase debe ser >= 0")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "salarioBase debe ser <= 15000000")
    private BigDecimal salarioBase;

    // opcional: si decido permitir crear credenciales aquí
    private String password;
}
