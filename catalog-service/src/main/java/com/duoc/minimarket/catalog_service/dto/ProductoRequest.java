package com.duoc.minimarket.catalog_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(

        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 40, message = "El SKU no puede superar 40 caracteres")
        String sku,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Size(
                max = 500,
                message = "La descripción no puede superar 500 caracteres"
        )
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El precio debe ser mayor que cero"
        )
        BigDecimal precio,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId
) {
}
