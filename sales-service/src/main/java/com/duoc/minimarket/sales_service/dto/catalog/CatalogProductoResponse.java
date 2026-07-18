package com.duoc.minimarket.sales_service.dto.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProductoResponse(
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