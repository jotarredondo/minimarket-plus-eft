package com.duoc.minimarket.catalog_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalidaVentaRequest(

        @NotNull(message = "La cantidad es obligatoria")
        @Min(
                value = 1,
                message = "La cantidad debe ser igual o superior a 1"
        )
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(
                max = 250,
                message = "El motivo no puede superar los 250 caracteres"
        )
        String motivo
) {
}
