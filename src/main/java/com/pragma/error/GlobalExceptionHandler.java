package com.pragma.error;

import com.pragma.domain.exception.EmailAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DuplicateKeyException;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Slf4j
@RestControllerAdvice
@Order(0)
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleBind(WebExchangeBindException ex) {
        String errorId = UUID.randomUUID().toString();
        var details = ex.getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("[{}] Validation error: {}", errorId, details);
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos", details, errorId));
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        String errorId = UUID.randomUUID().toString();
        String field = (ex.getField() == null || ex.getField().isBlank()) ? "email" : ex.getField();

        var details = List.of(new ValidationError(field, "ya existe"));

        log.warn("[{}] Conflict: {} (field={})", errorId, ex.getMessage(), field);

        return Mono.just(ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                details,
                errorId
        ));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
        String errorId = UUID.randomUUID().toString();
        var details = ex.getConstraintViolations().stream()
                .map(v -> new ValidationError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        log.warn("[{}] Constraint violation: {}", errorId, details);
        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos", details, errorId));
    }

    @ExceptionHandler({DuplicateKeyException.class, R2dbcDataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleDuplicate(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("[{}] Duplicate/conflict: {}", errorId, ex.getMessage());
        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT.value(),
                "Conflicto de datos", List.of(new ValidationError("global", "Registro duplicado o restricción de unicidad")),
                errorId));
    }

    @ExceptionHandler({BadSqlGrammarException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleDataAccess(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("[{}] Data access error", errorId, ex);
        return Mono.just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno de base de datos", List.of(), errorId));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String errorId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (status.is5xxServerError()) {
            log.error("[{}] {}", errorId, ex.getReason(), ex);
        } else {
            log.warn("[{}] {}", errorId, ex.getReason());
        }
        return Mono.just(ErrorResponse.of(status.value(), ex.getReason(), List.of(), errorId));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public Mono<ErrorResponse> handleErrorResponse(ErrorResponseException ex) {
        String errorId = UUID.randomUUID().toString();
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("[{}] {}", errorId, ex.getBody() != null ? ex.getBody().getDetail() : ex.getMessage(), ex);
        return Mono.just(ErrorResponse.of(status.value(),
                "Error procesando la solicitud", List.of(), errorId));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneric(Throwable ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("[{}] Unexpected error", errorId, ex);
        return Mono.just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error inesperado", List.of(), errorId));
    }

    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String errorId;
        private List<ValidationError> details;

        public static ErrorResponse of(int status, String error, List<ValidationError> details, String errorId) {
            return new ErrorResponse(Instant.now(), status, error, errorId, details);
        }
    }
}
/*@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleBind(WebExchangeBindException ex) {
        List<ValidationError> errors = ex.getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return Mono.just(ErrorResponse.of(400, "Datos inválidos", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ValidationError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        return Mono.just(ErrorResponse.of(400, "Datos inválidos", errors));
    }

    // Asegúrate de tener esta excepción en tu proyecto o cámbiala por IllegalArgumentException
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleDuplicate(EmailAlreadyExistsException ex) {
        return Mono.just(ErrorResponse.of(409, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return Mono.just(ErrorResponse.of(400, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneric(Exception ex) {
        log.error("[Unhandled]", ex);
        return Mono.just(ErrorResponse.of(
                500,
                "Ha ocurrido un error inesperado. Por favor intente nuevamente.",
                null
        ));
    }

    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private List<ValidationError> details;

        public static ErrorResponse of(int status, String error, List<ValidationError> details) {
            return new ErrorResponse(Instant.now(), status, error, details);
        }
    }
}*/