package com.pragma.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UsuarioRequest {

    @NotBlank(message = "{usuario.nombres.obligatorio}")
    private String nombres;

    @NotBlank(message = "{usuario.apellidos.obligatorio}")
    private String apellidos;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String direccion;
    private String telefono;

    @NotBlank(message = "{usuario.correo.obligatorio}")
    @Email(message = "{usuario.correo.formato}")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$",
            message = "{usuario.correo.formato}"
    )
    private String correoElectronico;

    @NotNull(message = "{usuario.salario.obligatorio}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{usuario.salario.min}")
    @DecimalMax(value = "15000000.0", inclusive = true, message = "{usuario.salario.max}")
    private BigDecimal salarioBase;

    @NotBlank(message = "{usuario.password.obligatorio}")
    private String password;
}
