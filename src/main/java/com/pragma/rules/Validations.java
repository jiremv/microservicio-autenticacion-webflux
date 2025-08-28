package com.pragma.rules;

public enum Validations {
    NOMBRES_OBLIGATORIO("nombres es obligatorio"),
    APELLIDOS_OBLIGATORIO("apellidos es obligatorio"),
    CORREO_OBLIGATORIO("correoElectronico es obligatorio"),
    CORREO_FORMATO("correoElectronico no tiene formato válido"),
    SALARIO_OBLIGATORIO("salarioBase es obligatorio"),
    SALARIO_MIN("salarioBase debe ser >= 0"),
    SALARIO_MAX("salarioBase debe ser <= 15000000");
    private final String mensaje;

    Validations(String mensaje) {
        this.mensaje = mensaje;
    }
    public String getMensaje() {
        return mensaje;
    }
    public static final String NOMBRES_OBLIGATORIO_MESSAGE = NOMBRES_OBLIGATORIO.mensaje;
    public static final String APELLIDOS_OBLIGATORIO_MESSAGE = APELLIDOS_OBLIGATORIO.mensaje;
    public static final String CORREO_OBLIGATORIO_MESSAGE = CORREO_OBLIGATORIO.mensaje;
    public static final String CORREO_FORMATO_MESSAGE = CORREO_FORMATO.mensaje;
    public static final String SALARIO_OBLIGATORIO_MESSAGE = SALARIO_OBLIGATORIO.mensaje;
    public static final String SALARIO_MIN_MESSAGE = SALARIO_MIN.mensaje;
    public static final String SALARIO_MAX_MESSAGE = SALARIO_MAX.mensaje;
}