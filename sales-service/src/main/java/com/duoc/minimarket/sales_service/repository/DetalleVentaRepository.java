package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Long> {
}
