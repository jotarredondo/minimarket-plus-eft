package com.duoc.minimarket.catalog_service.dto;

import java.time.LocalDateTime;

public record InventarioResponse(
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
