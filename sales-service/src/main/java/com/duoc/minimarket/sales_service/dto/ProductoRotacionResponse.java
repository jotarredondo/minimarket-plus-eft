package com.duoc.minimarket.sales_service.dto;

import java.math.BigDecimal;

public record ProductoRotacionResponse(
        Long productoId,
        String sku,
        String nombreProducto,
        long unidadesVendidas,
        BigDecimal totalVendido
) {
}