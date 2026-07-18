package com.duoc.minimarket.sales_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ventas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_venta_pedido",
                        columnNames = "pedido_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_venta_fecha",
                        columnList = "fechaVenta"
                ),
                @Index(
                        name = "idx_venta_cajero",
                        columnList = "cajero_email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(
            name = "cliente_email",
            nullable = false,
            length = 150
    )
    private String clienteEmail;

    @Column(
            name = "cajero_email",
            nullable = false,
            length = 150
    )
    private String cajeroEmail;

    @Column(nullable = false)
    private Long sucursalId;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

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
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.CONFIRMADA;

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
        recalcularTotales();
    }

    public void recalcularTotales() {
        subtotal = detalles.stream()
                .map(DetalleVenta::calcularSubtotalBruto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        descuento = detalles.stream()
                .map(DetalleVenta::getDescuentoSeguro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        total = subtotal
                .subtract(descuento)
                .max(BigDecimal.ZERO);
    }

    @PrePersist
    public void antesDeCrear() {
        if (fechaVenta == null) {
            fechaVenta = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoVenta.CONFIRMADA;
        }

        recalcularTotales();
    }
}
