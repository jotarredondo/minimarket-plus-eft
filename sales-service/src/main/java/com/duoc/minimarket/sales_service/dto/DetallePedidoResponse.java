package com.duoc.minimarket.sales_service.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long id,
        Long productoId,
        Long inventarioId,
        String sku,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal descuento,
        BigDecimal subtotal
) {
}