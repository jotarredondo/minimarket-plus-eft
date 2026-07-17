package com.duoc.minimarket.catalog_service.repository;

import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.entity.OrdenReposicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenReposicionRepository
        extends JpaRepository<OrdenReposicion, Long> {

    boolean existsByInventarioIdAndEstado(
            Long inventarioId,
            EstadoOrdenReposicion estado
    );

    List<OrdenReposicion> findByEstadoOrderByFechaGeneracionDesc(
            EstadoOrdenReposicion estado
    );

    List<OrdenReposicion>
    findByInventarioIdOrderByFechaGeneracionDesc(Long inventarioId);
}
