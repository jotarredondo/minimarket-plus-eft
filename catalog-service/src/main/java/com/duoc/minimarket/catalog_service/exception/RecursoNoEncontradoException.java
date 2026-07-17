package com.duoc.minimarket.catalog_service.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(
            String recurso,
            Object identificador
    ) {
        super(
                recurso
                        + " no encontrado con identificador: "
                        + identificador
        );
    }
}