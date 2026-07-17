package com.duoc.minimarket.catalog_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_reposicion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenReposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "inventario_id",
            nullable = false
    )
    private Inventario inventario;

    @Column(nullable = false)
    private Integer cantidadSugerida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrdenReposicion estado;

    @Column(nullable = false, length = 250)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoOrdenReposicion.GENERADA;
        }

        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
    }
}
