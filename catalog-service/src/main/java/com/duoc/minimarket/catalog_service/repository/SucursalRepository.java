package com.duoc.minimarket.catalog_service.repository;

import com.duoc.minimarket.catalog_service.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Sucursal> findByActivoTrueOrderByNombreAsc();
}
