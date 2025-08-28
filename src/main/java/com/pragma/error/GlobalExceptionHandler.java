package com.pragma.error;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.List;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleValidation(WebExchangeBindException ex) {
        List<ValidationError> details = ex.getFieldErrors().stream()
                .map(this::toValidationError)
                .toList();
        log.warn("[Validation] {} errores: {}", details.size(), details);
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Validación fallida", details));
    }
    private ValidationError toValidationError(FieldError fe) {
        return new ValidationError(fe.getField(), fe.getDefaultMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[Conflict] {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }
/*
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneric(Exception ex) {
        log.error("[Unhandled] {}", ex.getMessage(), ex);
        return Mono.just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido un error inesperado. Por favor intente nuevamente.", null));
    }*/
    @Data
    public static class ErrorResponse {
        private final Instant timestamp = Instant.now();
        private final int status;
        private final String error;
        private final List<ValidationError> details;
        static ErrorResponse of(int status, String error, List<ValidationError> details) {
            return new ErrorResponse(status, error, details);
        }
    }
    @Data
    public static class ValidationError {
        private final String field;
        private final String message;
    }
}
