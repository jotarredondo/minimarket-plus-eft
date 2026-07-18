package com.duoc.minimarket.sales_service.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogInventarioResponse(
        Long id,
        Long productoId,
        String productoSku,
        String productoNombre,
        Long sucursalId,
        String sucursalCodigo,
        String sucursalNombre,
        Integer stockActual,
        Integer stockMinimo,
        Boolean requiereReposicion,
        LocalDateTime fechaActualizacion
) {
}
