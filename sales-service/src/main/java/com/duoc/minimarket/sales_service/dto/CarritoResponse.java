package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.EstadoCarrito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CarritoResponse(
        Long id,
        String clienteEmail,
        Long sucursalId,
        EstadoCarrito estado,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<ItemCarritoResponse> items
) {
}
