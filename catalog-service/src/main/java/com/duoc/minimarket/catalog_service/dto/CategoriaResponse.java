package com.duoc.minimarket.catalog_service.dto;

public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo
) {
}
