package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long carritoId,
        String clienteEmail,
        Long sucursalId,
        TipoEntrega tipoEntrega,
        String direccionEntrega,
        EstadoPedido estado,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<DetallePedidoResponse> detalles
) {
}
