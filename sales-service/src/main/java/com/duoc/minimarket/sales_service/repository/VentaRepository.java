package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepository
        extends JpaRepository<Venta, Long> {

    boolean existsByPedidoId(Long pedidoId);

    Optional<Venta> findByPedidoId(Long pedidoId);

    List<Venta> findAllByOrderByFechaVentaDesc();

    List<Venta> findByCajeroEmailOrderByFechaVentaDesc(
            String cajeroEmail
    );
}
