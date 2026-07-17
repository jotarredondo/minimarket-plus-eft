package com.duoc.minimarket.catalog_service.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Boolean activo,
        Long categoriaId,
        String categoriaNombre
) {
}
