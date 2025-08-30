package com.pragma.error;

import com.pragma.domain.exception.EmailAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
@Order(-2) // que se ejecute antes que handlers por defecto
public class GlobalExceptionHandler {
    private static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(EmailAlreadyExistsException ex, ServerWebExchange exchange) {
        var pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El correo electrónico ya está registrado"
        );
        // Lo que espera el test:
        pd.setTitle("Conflicto: recurso ya existe");
        pd.setType(URI.create("https://tu-dominio.com/probs/duplicate-email"));
        pd.setProperty("code", AppErrorCode.DUPLICATE_EMAIL.name());
        // Usa el nombre del campo que valida el test
        pd.setProperty("errors", List.of(Map.of(
                "field", "correoElectronico",
                "message", "ya existe",
                "rejectedValue", ex.getCorreo()
        )));
        attachCommon(exchange, pd); // agrega errorId, timestamp, instance y header
        return pd;
    }

    /*@ExceptionHandler(CorreoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(CorreoDuplicadoException ex, ServerWebExchange exchange) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El correo electrónico ya está registrado");
        pd.setTitle("Conflicto de datos");
        pd.setType(URI.create("https://tu-dominio.com/probs/duplicate-email"));
        pd.setProperty("code", "DUPLICATE_EMAIL");

        // detalle de campo (opcional pero útil)
        pd.setProperty("errors", List.of(Map.of(
                "field", "email",
                "message", "ya existe",
                "rejectedValue", ex.getCorreo()
        )));

        attachCommon(exchange, pd); // tu helper que agrega errorId, timestamp, instance y header
        return pd;
    }*/

    // 409 - Email duplicado
    /*@ExceptionHandler(CorreoDuplicadoException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleCorreoDuplicado(
            CorreoDuplicadoException ex, ServerWebExchange exchange) {

        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflicto: recurso ya existe");
        pd.setType(URI.create("https://tu-dominio.com/probs/email-exists"));
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("code", AppErrorCode.DUPLICATE_EMAIL.name());
        pd.setProperty("errors", List.of(Map.of(
                "field", "correoElectronico",
                "message", "ya existe"
        )));
        // (Opcional) pista del recurso en conflicto:
        if (ex.getExistenteId() != null) {
            pd.setProperty("conflictResource", "/api/v1/usuarios/" + ex.getExistenteId());
        }

        var errorId = ex.getErrorId() != null ? ex.getErrorId() : UUID.randomUUID().toString();

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .header("X-Error-Id", errorId)
                .contentType(PROBLEM_JSON)
                .body(pd));
    }


    // 409 por clave única duplicada vía DB
    @ExceptionHandler({
            org.springframework.dao.DuplicateKeyException.class,
            org.springframework.dao.DataIntegrityViolationException.class,
            io.r2dbc.spi.R2dbcDataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDbDuplicate(RuntimeException ex, ServerWebExchange exchange) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El correo electrónico ya está registrado");
        pd.setType(URI.create("https://tu-dominio.com/probs/duplicate-email"));
        pd.setTitle("Conflicto de datos");
        pd.setProperty("code", "DUPLICATE_EMAIL");
        attachCommon(exchange, pd);
        return pd;
    }*/

    // 400 - Errores de validación @Valid (WebFlux)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidation(WebExchangeBindException ex,
                                                                ServerWebExchange exchange) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud tiene errores de validación.");
        pd.setTitle("Solicitud inválida");
        pd.setType(URI.create("https://tu-dominio.com/probs/constraint-violation"));
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("code", AppErrorCode.VALIDATION_ERROR.name());

        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toError)
                .toList();
        pd.setProperty("errors", errors);

        return Mono.just(ResponseEntity.badRequest()
                .contentType(PROBLEM_JSON)
                .body(pd));
    }

    // 400 - Validaciones imperativas (opcional)
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleConstraintViolation(ConstraintViolationException ex,
                                                                         ServerWebExchange exchange) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La solicitud contiene valores inválidos.");
        pd.setTitle("Solicitud inválida");
        pd.setType(URI.create("https://tu-dominio.com/probs/constraint-violation"));
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("code", AppErrorCode.VALIDATION_ERROR.name());

        var errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", String.valueOf(cv.getPropertyPath()),
                        "message", cv.getMessage(),
                        "rejectedValue", Objects.toString(cv.getInvalidValue(), "null")
                ))
                .toList();
        pd.setProperty("errors", errors);

        return Mono.just(ResponseEntity.badRequest()
                .contentType(PROBLEM_JSON)
                .body(pd));
    }

    // 404 - Not found centralizado (si usas ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleResponseStatus(ResponseStatusException ex,
                                                                    ServerWebExchange exchange) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                    Optional.ofNullable(ex.getReason()).orElse("El recurso no existe."));
            pd.setTitle("No encontrado");
            pd.setType(URI.create("https://tu-dominio.com/probs/not-found"));
            pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
            pd.setProperty("timestamp", OffsetDateTime.now().toString());
            pd.setProperty("code", AppErrorCode.RESOURCE_NOT_FOUND.name());
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(PROBLEM_JSON)
                    .body(pd));
        }
        return handleGeneric(ex, exchange);
    }

    // 500 - Catch-all
    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGeneric(Throwable ex, ServerWebExchange exchange) {
        var errorId = UUID.randomUUID().toString();

        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado.");
        pd.setTitle("Error interno");
        pd.setType(URI.create("https://tu-dominio.com/probs/internal-error"));
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("code", AppErrorCode.INTERNAL_ERROR.name());
        pd.setProperty("errorId", errorId);

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error-Id", errorId)
                .contentType(PROBLEM_JSON)
                .body(pd));
    }
    private Map<String, Object> toError(FieldError fe) {
        var map = new LinkedHashMap<String, Object>();
        map.put("field", fe.getField());
        map.put("message", fe.getDefaultMessage());
        map.put("rejectedValue", fe.getRejectedValue());
        return map;
    }
    private void attachCommon(ServerWebExchange exchange, ProblemDetail pd) {
        // id único para correlación
        String errorId = UUID.randomUUID().toString();
        pd.setProperty("errorId", errorId);
        pd.setProperty("timestamp", Instant.now().toString());

        // URL del request como "instance" (RFC 7807)
        pd.setInstance(URI.create(exchange.getRequest().getPath().value()));

        // también lo exponemos como header útil para logs/tracing
        exchange.getResponse().getHeaders().add("X-Error-Id", errorId);
    }
}