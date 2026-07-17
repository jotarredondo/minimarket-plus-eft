package com.duoc.minimarket.catalog_service.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CrearInventarioRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId,

        @NotNull(message = "El stock inicial es obligatorio")
        @PositiveOrZero(message = "El stock inicial no puede ser negativo")
        Integer stockInicial,

        @NotNull(message = "El stock mínimo es obligatorio")
        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {
}
