package com.duoc.minimarket.sales_service.dto;

import com.duoc.minimarket.sales_service.entity.TipoPromocion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 120,
                message = "El nombre no puede superar 120 caracteres"
        )
        String nombre,

        @Size(
                max = 300,
                message = "La descripción no puede superar 300 caracteres"
        )
        String descripcion,

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser válido")
        Long productoId,

        @NotNull(message = "El tipo de promoción es obligatorio")
        TipoPromocion tipo,

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El valor debe ser superior a cero"
        )
        BigDecimal valor,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de término es obligatoria")
        LocalDateTime fechaFin
) {
}
