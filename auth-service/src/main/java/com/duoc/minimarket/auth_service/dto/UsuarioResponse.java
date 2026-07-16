package com.duoc.minimarket.auth_service.dto;

import com.duoc.minimarket.auth_service.entity.Rol;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
}
