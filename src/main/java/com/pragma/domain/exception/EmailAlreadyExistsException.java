package com.pragma.domain.exception;

import lombok.Getter;
import java.util.UUID;
@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final String correo;
    //private final UUID existenteId;
    //private final String errorId;

    /*public EmailAlreadyExistsException(String correo, UUID existenteId, String errorId) {
        super("El correo electrónico ya está registrado.");
        this.correo = correo;
        this.existenteId = existenteId;
        this.errorId = UUID.randomUUID().toString();
    }*/
    public EmailAlreadyExistsException(String message, String correo) {
        super(message);
        this.correo = correo;
    }

    /*public EmailAlreadyExistsException(String message, String correo, String errorId) {
        super(message);
        this.correo = correo;
        this.errorId = errorId;
    }*/

}
