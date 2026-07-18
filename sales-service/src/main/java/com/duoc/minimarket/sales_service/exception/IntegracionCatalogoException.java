package com.duoc.minimarket.sales_service.exception;


public class IntegracionCatalogoException
        extends RuntimeException {

    public IntegracionCatalogoException(String mensaje) {
        super(mensaje);
    }

    public IntegracionCatalogoException(
            String mensaje,
            Throwable causa
    ) {
        super(mensaje, causa);
    }
}
