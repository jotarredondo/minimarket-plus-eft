package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Long id,
        Long pedidoId,
        String clienteEmail,
        String cajeroEmail,
        Long sucursalId,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        EstadoVenta estado,
        LocalDateTime fechaVenta,
        List<DetalleVentaResponse> detalles
) {
}
