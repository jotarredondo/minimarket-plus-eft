package com.duoc.minimarket.auth_service.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("El email o la contraseña son incorrectos");
    }
}
