package com.duoc.minimarket.catalog_service.repository;

import com.duoc.minimarket.catalog_service.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByActivoTrueOrderByNombreAsc();
}
