package com.pragma.domain.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final String field;
    public EmailAlreadyExistsException(String message, String field) {
        super(message);
        this.field = field;
    }
}
