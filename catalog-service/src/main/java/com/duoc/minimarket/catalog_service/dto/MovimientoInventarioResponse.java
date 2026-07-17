package com.duoc.minimarket.catalog_service.dto;

import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        Long inventarioId,
        TipoMovimientoInventario tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        String motivo,
        String usuarioEmail,
        LocalDateTime fechaMovimiento
) {
}
