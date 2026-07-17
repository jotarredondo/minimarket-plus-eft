package com.duoc.minimarket.catalog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalRequest(

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 30, message = "El código no puede superar 30 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(
                max = 250,
                message = "La dirección no puede superar 250 caracteres"
        )
        String direccion
) {
}
