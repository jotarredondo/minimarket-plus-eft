package com.duoc.minimarket.catalog_service.dto;

public record SucursalResponse(
        Long id,
        String codigo,
        String nombre,
        String direccion,
        Boolean activo
) {
}
