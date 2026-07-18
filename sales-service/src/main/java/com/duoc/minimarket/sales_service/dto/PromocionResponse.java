package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.TipoPromocion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionResponse(
        Long id,
        String nombre,
        String descripcion,
        Long productoId,
        TipoPromocion tipo,
        BigDecimal valor,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean activo
) {
}
