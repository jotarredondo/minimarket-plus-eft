package com.duoc.minimarket.sales_service.dto;

import java.util.List;

public record ReporteRotacionResponse(
        ProductoRotacionResponse productoMasVendido,
        ProductoRotacionResponse productoMenosVendido,
        List<ProductoRotacionResponse> ranking
) {
}
