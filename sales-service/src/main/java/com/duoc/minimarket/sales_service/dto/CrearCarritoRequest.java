package com.duoc.minimarket.sales_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearCarritoRequest(

        @NotNull(message = "La sucursal es obligatoria")
        @Positive(message = "La sucursal debe ser válida")
        Long sucursalId
) {
}
