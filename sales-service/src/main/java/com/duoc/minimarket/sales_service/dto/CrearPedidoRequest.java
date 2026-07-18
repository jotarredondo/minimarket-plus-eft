package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearPedidoRequest(

        @NotNull(message = "El tipo de entrega es obligatorio")
        TipoEntrega tipoEntrega,

        @Size(
                max = 300,
                message = "La dirección no puede superar los 300 caracteres"
        )
        String direccionEntrega
) {
}
