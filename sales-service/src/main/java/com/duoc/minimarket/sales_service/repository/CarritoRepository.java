package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository
        extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByClienteEmailAndEstado(
            String clienteEmail,
            EstadoCarrito estado
    );

    Optional<Carrito> findByIdAndClienteEmail(
            Long id,
            String clienteEmail
    );

    List<Carrito> findByClienteEmailOrderByFechaCreacionDesc(
            String clienteEmail
    );
}
