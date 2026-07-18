package com.duoc.minimarket.sales_service.exception;

public class OperacionInvalidaException
        extends RuntimeException {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
