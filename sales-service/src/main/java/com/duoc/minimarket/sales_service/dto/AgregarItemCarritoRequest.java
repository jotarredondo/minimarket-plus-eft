package com.duoc.minimarket.sales_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarItemCarritoRequest(

        @NotNull(message = "El inventario es obligatorio")
        @Positive(message = "El inventario debe ser válido")
        Long inventarioId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(
                value = 1,
                message = "La cantidad debe ser igual o superior a 1"
        )
        Integer cantidad
) {
}
