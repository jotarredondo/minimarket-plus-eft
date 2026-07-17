package com.duoc.minimarket.catalog_service.repository;

import com.duoc.minimarket.catalog_service.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByCategoriaIdAndActivoTrueOrderByNombreAsc(
            Long categoriaId
    );
}

