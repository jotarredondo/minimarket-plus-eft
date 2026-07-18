package com.duoc.minimarket.sales_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCantidadItemRequest(

        @NotNull(message = "La cantidad es obligatoria")
        @Min(
                value = 1,
                message = "La cantidad debe ser igual o superior a 1"
        )
        Integer cantidad
) {
}