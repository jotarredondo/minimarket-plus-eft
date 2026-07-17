package com.duoc.minimarket.catalog_service.dto;

import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;

import java.time.LocalDateTime;

public record OrdenReposicionResponse(
        Long id,
        Long inventarioId,
        Long productoId,
        String productoNombre,
        Long sucursalId,
        String sucursalNombre,
        Integer cantidadSugerida,
        EstadoOrdenReposicion estado,
        String motivo,
        LocalDateTime fechaGeneracion
) {
}
