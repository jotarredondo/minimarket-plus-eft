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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
        name = "carritos",
        indexes = {
                @Index(
                        name = "idx_carrito_cliente_estado",
                        columnList = "cliente_email, estado"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "cliente_email",
            nullable = false,
            length = 150
    )
    private String clienteEmail;

    @Column(nullable = false)
    private Long sucursalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCarrito estado = EstadoCarrito.ACTIVO;

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

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(
            mappedBy = "carrito",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ItemCarrito> items = new ArrayList<>();

    public void agregarItem(ItemCarrito item) {
        item.setCarrito(this);
        items.add(item);
        recalcularTotales();
    }

    public void removerItem(ItemCarrito item) {
        items.remove(item);
        item.setCarrito(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        subtotal = items.stream()
                .map(ItemCarrito::calcularSubtotalBruto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        descuento = items.stream()
                .map(ItemCarrito::getDescuentoSeguro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        total = subtotal
                .subtract(descuento)
                .max(BigDecimal.ZERO);
    }

    @PrePersist
    public void antesDeCrear() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaCreacion = ahora;
        fechaActualizacion = ahora;

        if (estado == null) {
            estado = EstadoCarrito.ACTIVO;
        }

        recalcularTotales();
    }

    @PreUpdate
    public void antesDeActualizar() {
        fechaActualizacion = LocalDateTime.now();
        recalcularTotales();
    }
}
