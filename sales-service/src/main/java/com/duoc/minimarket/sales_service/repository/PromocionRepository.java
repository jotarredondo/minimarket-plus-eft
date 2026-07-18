package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PromocionRepository
        extends JpaRepository<Promocion, Long> {

    List<Promocion> findByActivoTrueOrderByFechaInicioDesc();

    List<Promocion>
    findByProductoIdAndActivoTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Long productoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
