package com.duoc.minimarket.catalog_service.dto;

import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import jakarta.validation.constraints.NotNull;

public record ActualizarOrdenReposicionRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoOrdenReposicion estado
) {
}
