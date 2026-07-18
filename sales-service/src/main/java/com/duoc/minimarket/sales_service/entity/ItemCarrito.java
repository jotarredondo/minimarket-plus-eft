package com.duoc.minimarket.sales_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "items_carrito",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_carrito_inventario",
                        columnNames = {
                                "carrito_id",
                                "inventario_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "carrito_id",
            nullable = false
    )
    private Carrito carrito;

    @Column(nullable = false)
    private Long productoId;

    @Column(name = "inventario_id", nullable = false)
    private Long inventarioId;

    @Column(nullable = false, length = 60)
    private String sku;

    @Column(nullable = false, length = 150)
    private String nombreProducto;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    public BigDecimal calcularSubtotalBruto() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }

        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );
    }

    public BigDecimal getDescuentoSeguro() {
        return descuento == null
                ? BigDecimal.ZERO
                : descuento;
    }

    public void recalcularSubtotal() {
        subtotal = calcularSubtotalBruto()
                .subtract(getDescuentoSeguro())
                .max(BigDecimal.ZERO);
    }

    @PrePersist
    @PreUpdate
    public void antesDeGuardar() {
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }

        recalcularSubtotal();
    }
}
