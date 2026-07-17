package com.duoc.minimarket.catalog_service.repository;

import com.duoc.minimarket.catalog_service.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoIdAndSucursalId(
            Long productoId,
            Long sucursalId
    );

    boolean existsByProductoIdAndSucursalId(
            Long productoId,
            Long sucursalId
    );

    List<Inventario> findByProductoIdOrderBySucursalNombreAsc(
            Long productoId
    );

    List<Inventario> findBySucursalIdOrderByProductoNombreAsc(
            Long sucursalId
    );
}
