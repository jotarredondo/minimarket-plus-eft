package com.duoc.minimarket.catalog_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(
                max = 250,
                message = "La descripción no puede superar 250 caracteres"
        )
        String descripcion
) {
}
