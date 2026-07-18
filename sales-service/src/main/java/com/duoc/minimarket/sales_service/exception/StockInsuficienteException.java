package com.duoc.minimarket.sales_service.exception;

public class StockInsuficienteException
        extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
