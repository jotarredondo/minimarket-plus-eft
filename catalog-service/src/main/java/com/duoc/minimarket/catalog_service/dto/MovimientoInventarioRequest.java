package com.duoc.minimarket.catalog_service.dto;


import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MovimientoInventarioRequest(

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimientoInventario tipo,

        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 250, message = "El motivo no puede superar 250 caracteres")
        String motivo
) {
}
