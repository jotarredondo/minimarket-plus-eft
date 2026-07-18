package com.duoc.minimarket.sales_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(
            RecursoDuplicadoException exception,
            HttpServletRequest request
    ) {
        return crearRespuesta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
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
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(IntegracionCatalogoException.class)
    public ResponseEntity<ApiError> manejarIntegracionCatalogo(
            IntegracionCatalogoException exception,
            HttpServletRequest request
    ) {
        return crearRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request,
                errores
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> manejarRestricciones(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        errores.put(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                );

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Uno o más parámetros no son válidos",
                request,
                errores
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request,
                null
        );
    }

    private ResponseEntity<ApiError> crearRespuesta(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request,
            Map<String, String> errores
    ) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                errores
        );

        return ResponseEntity
                .status(status)
                .body(apiError);
    }
}
