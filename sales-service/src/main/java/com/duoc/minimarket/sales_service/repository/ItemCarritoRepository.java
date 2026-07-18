package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCarritoRepository
        extends JpaRepository<ItemCarrito, Long> {

    Optional<ItemCarrito> findByCarritoIdAndInventarioId(
            Long carritoId,
            Long inventarioId
    );

    Optional<ItemCarrito> findByIdAndCarritoClienteEmail(
            Long itemId,
            String clienteEmail
    );
}
