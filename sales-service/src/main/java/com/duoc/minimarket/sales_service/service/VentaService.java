package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.DetalleVentaResponse;
import com.duoc.minimarket.sales_service.dto.VentaResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogSalidaVentaRequest;
import com.duoc.minimarket.sales_service.entity.DetallePedido;
import com.duoc.minimarket.sales_service.entity.DetalleVenta;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.Pedido;
import com.duoc.minimarket.sales_service.entity.Venta;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
import com.duoc.minimarket.sales_service.repository.PedidoRepository;
import com.duoc.minimarket.sales_service.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VentaService {

    private final PedidoRepository pedidoRepository;
    private final VentaRepository ventaRepository;
    private final CatalogClient catalogClient;

    public VentaService(
            PedidoRepository pedidoRepository,
            VentaRepository ventaRepository,
            CatalogClient catalogClient
    ) {
        this.pedidoRepository = pedidoRepository;
        this.ventaRepository = ventaRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public VentaResponse confirmarVenta(
            Long pedidoId,
            String cajeroEmail,
            String authorizationHeader
    ) {
        String emailCajero =
                normalizarEmail(cajeroEmail);

        validarAuthorizationHeader(
                authorizationHeader
        );

        if (ventaRepository.existsByPedidoId(pedidoId)) {
            throw new RecursoDuplicadoException(
                    "El pedido ya posee una venta confirmada"
            );
        }

        Pedido pedido = pedidoRepository
                .findById(pedidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Pedido no encontrado: "
                                        + pedidoId
                        )
                );

        validarPedidoParaVenta(pedido);

        /*
         * Primera pasada:
         * se valida todo el inventario antes de descontar stock.
         */
        for (DetallePedido detalle : pedido.getDetalles()) {
            CatalogInventarioResponse inventario =
                    catalogClient.obtenerInventario(
                            detalle.getInventarioId(),
                            authorizationHeader
                    );

            validarInventario(
                    pedido,
                    detalle,
                    inventario
            );
        }

        /*
         * Segunda pasada:
         * solo después de validar todos los productos,
         * se registran las salidas de inventario.
         */
        for (DetallePedido detalle : pedido.getDetalles()) {
            CatalogSalidaVentaRequest salidaRequest =
                    new CatalogSalidaVentaRequest(
                            detalle.getCantidad(),
                            "Venta asociada al pedido "
                                    + pedido.getId()
                    );

            catalogClient.registrarSalidaVenta(
                    detalle.getInventarioId(),
                    salidaRequest,
                    authorizationHeader
            );
        }

        Venta venta = Venta.builder()
                .pedidoId(pedido.getId())
                .clienteEmail(pedido.getClienteEmail())
                .cajeroEmail(emailCajero)
                .sucursalId(pedido.getSucursalId())
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .estado(EstadoVenta.CONFIRMADA)
                .build();

        for (DetallePedido detallePedido
                : pedido.getDetalles()) {

            DetalleVenta detalleVenta =
                    DetalleVenta.builder()
                            .productoId(
                                    detallePedido.getProductoId()
                            )
                            .inventarioId(
                                    detallePedido.getInventarioId()
                            )
                            .sku(detallePedido.getSku())
                            .nombreProducto(
                                    detallePedido.getNombreProducto()
                            )
                            .precioUnitario(
                                    detallePedido.getPrecioUnitario()
                            )
                            .cantidad(
                                    detallePedido.getCantidad()
                            )
                            .descuento(
                                    detallePedido
                                            .getDescuentoSeguro()
                            )
                            .subtotal(BigDecimal.ZERO)
                            .build();

            venta.agregarDetalle(detalleVenta);
        }

        Venta ventaGuardada =
                ventaRepository.save(venta);

        pedido.setEstado(EstadoPedido.COMPLETADO);
        pedidoRepository.save(pedido);

        return convertirResponse(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(
            Long ventaId
    ) {
        Venta venta = ventaRepository
                .findById(ventaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Venta no encontrada: "
                                        + ventaId
                        )
                );

        return convertirResponse(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarTodas() {
        return ventaRepository
                .findAllByOrderByFechaVentaDesc()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarPorCajero(
            String cajeroEmail
    ) {
        return ventaRepository
                .findByCajeroEmailOrderByFechaVentaDesc(
                        normalizarEmail(cajeroEmail)
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private void validarPedidoParaVenta(
            Pedido pedido
    ) {
        if (pedido.getEstado()
                != EstadoPedido.PENDIENTE) {

            throw new OperacionInvalidaException(
                    "Solamente se pueden confirmar "
                            + "pedidos pendientes"
            );
        }

        if (pedido.getDetalles() == null
                || pedido.getDetalles().isEmpty()) {

            throw new OperacionInvalidaException(
                    "El pedido no contiene productos"
            );
        }
    }

    private void validarInventario(
            Pedido pedido,
            DetallePedido detalle,
            CatalogInventarioResponse inventario
    ) {
        if (inventario.id() == null
                || !inventario.id()
                .equals(detalle.getInventarioId())) {

            throw new OperacionInvalidaException(
                    "El inventario del pedido no es válido"
            );
        }

        if (inventario.productoId() == null
                || !inventario.productoId()
                .equals(detalle.getProductoId())) {

            throw new OperacionInvalidaException(
                    "El producto no coincide con el inventario"
            );
        }

        if (inventario.sucursalId() == null
                || !inventario.sucursalId()
                .equals(pedido.getSucursalId())) {

            throw new OperacionInvalidaException(
                    "El inventario no pertenece a la "
                            + "sucursal del pedido"
            );
        }

        int stockDisponible =
                inventario.stockActual() == null
                        ? 0
                        : inventario.stockActual();

        if (detalle.getCantidad() > stockDisponible) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para "
                            + detalle.getNombreProducto()
                            + ". Disponible: "
                            + stockDisponible
                            + ", solicitado: "
                            + detalle.getCantidad()
            );
        }
    }

    private void validarAuthorizationHeader(
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || authorizationHeader.isBlank()
                || !authorizationHeader
                .startsWith("Bearer ")) {

            throw new OperacionInvalidaException(
                    "No fue posible reenviar el JWT "
                            + "a Catalog Service"
            );
        }
    }

    private String normalizarEmail(
            String email
    ) {
        if (email == null || email.isBlank()) {
            throw new OperacionInvalidaException(
                    "No fue posible identificar al cajero"
            );
        }

        return email.trim().toLowerCase();
    }

    private VentaResponse convertirResponse(
            Venta venta
    ) {
        List<DetalleVentaResponse> detalles =
                venta.getDetalles()
                        .stream()
                        .map(this::convertirDetalleResponse)
                        .toList();

        return new VentaResponse(
                venta.getId(),
                venta.getPedidoId(),
                venta.getClienteEmail(),
                venta.getCajeroEmail(),
                venta.getSucursalId(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getFechaVenta(),
                detalles
        );
    }

    private DetalleVentaResponse convertirDetalleResponse(
            DetalleVenta detalle
    ) {
        return new DetalleVentaResponse(
                detalle.getId(),
                detalle.getProductoId(),
                detalle.getInventarioId(),
                detalle.getSku(),
                detalle.getNombreProducto(),
                detalle.getPrecioUnitario(),
                detalle.getCantidad(),
                detalle.getDescuento(),
                detalle.getSubtotal()
        );
    }
}
