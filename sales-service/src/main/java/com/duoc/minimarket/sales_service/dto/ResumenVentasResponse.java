package com.duoc.minimarket.sales_service.dto;

import java.math.BigDecimal;

public record ResumenVentasResponse(
        long cantidadVentas,
        BigDecimal subtotalAcumulado,
        BigDecimal descuentosAcumulados,
        BigDecimal totalVendido
) {
}
