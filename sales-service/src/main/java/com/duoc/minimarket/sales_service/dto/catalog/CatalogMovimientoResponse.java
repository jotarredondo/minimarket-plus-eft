package com.duoc.minimarket.sales_service.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogMovimientoResponse(
        Long id,
        Long inventarioId,
        String tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        String motivo,
        String usuarioEmail,
        LocalDateTime fechaMovimiento
) {
}
