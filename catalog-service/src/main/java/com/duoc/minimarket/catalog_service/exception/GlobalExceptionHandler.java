package com.duoc.minimarket.catalog_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarRecursoDuplicado(
            RecursoDuplicadoException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler({
            OperacionInvalidaException.class,
            StockInsuficienteException.class
    })
    public ResponseEntity<ApiError> manejarOperacionInvalida(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request.getRequestURI(),
                errores
        );
    }

    private ResponseEntity<ApiError> construirRespuesta(
            HttpStatus status,
            String mensaje,
            String path,
            Map<String, String> erroresValidacion
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                path,
                erroresValidacion
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
