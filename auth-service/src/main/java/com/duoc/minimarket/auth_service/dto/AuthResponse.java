package com.duoc.minimarket.auth_service.dto;

public record AuthResponse(
        String token,
        String tipo,
        long expiresInSeconds,
        UsuarioResponse usuario
) {
}