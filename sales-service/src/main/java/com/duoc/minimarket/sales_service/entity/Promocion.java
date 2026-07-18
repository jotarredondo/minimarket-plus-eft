package com.duoc.minimarket.sales_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "promociones",
        indexes = {
                @Index(
                        name = "idx_promocion_producto_activo",
                        columnList = "productoId, activo"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Long productoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPromocion tipo;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    public boolean esVigente(LocalDateTime momento) {
        return Boolean.TRUE.equals(activo)
                && momento != null
                && !momento.isBefore(fechaInicio)
                && !momento.isAfter(fechaFin);
    }

    public BigDecimal calcularDescuento(
            BigDecimal precioUnitario,
            Integer cantidad,
            LocalDateTime momento
    ) {
        if (!esVigente(momento)
                || precioUnitario == null
                || cantidad == null
                || cantidad <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );

        BigDecimal descuento;

        if (tipo == TipoPromocion.PORCENTAJE) {
            descuento = subtotal
                    .multiply(valor)
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        } else {
            descuento = valor.multiply(
                    BigDecimal.valueOf(cantidad)
            );
        }

        return descuento.min(subtotal);
    }

    @PrePersist
    public void antesDeCrear() {
        if (activo == null) {
            activo = true;
        }
    }
}
