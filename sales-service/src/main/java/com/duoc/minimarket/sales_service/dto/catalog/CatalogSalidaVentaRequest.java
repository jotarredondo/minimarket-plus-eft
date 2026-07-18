package com.duoc.minimarket.sales_service.dto.catalog;

public record CatalogSalidaVentaRequest(
        Integer cantidad,
        String motivo
) {
}