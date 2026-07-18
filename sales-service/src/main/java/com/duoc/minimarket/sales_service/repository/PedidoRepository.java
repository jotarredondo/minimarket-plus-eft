package com.duoc.minimarket.sales_service.repository;

import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteEmailOrderByFechaCreacionDesc(
            String clienteEmail
    );

    List<Pedido> findByEstadoOrderByFechaCreacionAsc(
            EstadoPedido estado
    );

    Optional<Pedido> findByIdAndClienteEmail(
            Long id,
            String clienteEmail
    );
}