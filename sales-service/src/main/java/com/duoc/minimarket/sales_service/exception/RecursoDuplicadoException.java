package com.duoc.minimarket.sales_service.exception;

public class RecursoDuplicadoException
        extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
