package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.CrearPedidoRequest;
import com.duoc.minimarket.sales_service.dto.DetallePedidoResponse;
import com.duoc.minimarket.sales_service.dto.PedidoResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.DetallePedido;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.ItemCarrito;
import com.duoc.minimarket.sales_service.entity.Pedido;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
import com.duoc.minimarket.sales_service.repository.CarritoRepository;
import com.duoc.minimarket.sales_service.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final CatalogClient catalogClient;

    public PedidoService(
            CarritoRepository carritoRepository,
            PedidoRepository pedidoRepository,
            CatalogClient catalogClient
    ) {
        this.carritoRepository = carritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public PedidoResponse crearDesdeCarrito(
            String clienteEmail,
            CrearPedidoRequest request,
            String authorizationHeader
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        validarAuthorizationHeader(authorizationHeader);
        validarDatosEntrega(request);

        Carrito carrito =
                carritoRepository
                        .findByClienteEmailAndEstado(
                                emailNormalizado,
                                EstadoCarrito.ACTIVO
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El cliente no posee un carrito activo"
                                )
                        );

        if (carrito.getItems() == null
                || carrito.getItems().isEmpty()) {

            throw new OperacionInvalidaException(
                    "No es posible crear un pedido con un carrito vacío"
            );
        }

        Pedido pedido = Pedido.builder()
                .carritoId(carrito.getId())
                .clienteEmail(emailNormalizado)
                .sucursalId(carrito.getSucursalId())
                .tipoEntrega(request.tipoEntrega())
                .direccionEntrega(
                        obtenerDireccionNormalizada(request)
                )
                .estado(EstadoPedido.PENDIENTE)
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido detalle =
                    crearDetalleValidado(
                            carrito,
                            item,
                            authorizationHeader
                    );

            pedido.agregarDetalle(detalle);
        }

        Pedido pedidoGuardado =
                pedidoRepository.save(pedido);

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carritoRepository.save(carrito);

        return convertirResponse(pedidoGuardado);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosCliente(
            String clienteEmail
    ) {
        return pedidoRepository
                .findByClienteEmailOrderByFechaCreacionDesc(
                        normalizarEmail(clienteEmail)
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoCliente(
            Long pedidoId,
            String clienteEmail
    ) {
        Pedido pedido =
                pedidoRepository
                        .findByIdAndClienteEmail(
                                pedidoId,
                                normalizarEmail(clienteEmail)
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pedido no encontrado: "
                                                + pedidoId
                                )
                        );

        return convertirResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPendientes() {
        return pedidoRepository
                .findByEstadoOrderByFechaCreacionAsc(
                        EstadoPedido.PENDIENTE
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorIdGestion(
            Long pedidoId
    ) {
        Pedido pedido =
                pedidoRepository
                        .findById(pedidoId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pedido no encontrado: "
                                                + pedidoId
                                )
                        );

        return convertirResponse(pedido);
    }

    private DetallePedido crearDetalleValidado(
            Carrito carrito,
            ItemCarrito item,
            String authorizationHeader
    ) {
        CatalogInventarioResponse inventario =
                catalogClient.obtenerInventario(
                        item.getInventarioId(),
                        authorizationHeader
                );

        validarInventario(
                carrito,
                item,
                inventario
        );

        CatalogProductoResponse producto =
                catalogClient.obtenerProducto(
                        item.getProductoId(),
                        authorizationHeader
                );

        validarProducto(item, producto);

        return DetallePedido.builder()
                .productoId(producto.id())
                .inventarioId(inventario.id())
                .sku(producto.sku())
                .nombreProducto(producto.nombre())
                .precioUnitario(producto.precio())
                .cantidad(item.getCantidad())
                .descuento(
                        item.getDescuentoSeguro()
                )
                .subtotal(BigDecimal.ZERO)
                .build();
    }

    private void validarInventario(
            Carrito carrito,
            ItemCarrito item,
            CatalogInventarioResponse inventario
    ) {
        if (inventario.id() == null
                || !inventario.id()
                .equals(item.getInventarioId())) {

            throw new OperacionInvalidaException(
                    "El inventario del carrito no es válido"
            );
        }

        if (inventario.productoId() == null
                || !inventario.productoId()
                .equals(item.getProductoId())) {

            throw new OperacionInvalidaException(
                    "El producto no coincide con el inventario"
            );
        }

        if (inventario.sucursalId() == null
                || !inventario.sucursalId()
                .equals(carrito.getSucursalId())) {

            throw new OperacionInvalidaException(
                    "El inventario no pertenece a la sucursal del carrito"
            );
        }

        int stockDisponible =
                inventario.stockActual() == null
                        ? 0
                        : inventario.stockActual();

        if (item.getCantidad() > stockDisponible) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para "
                            + item.getNombreProducto()
                            + ". Disponible: "
                            + stockDisponible
                            + ", solicitado: "
                            + item.getCantidad()
            );
        }
    }

    private void validarProducto(
            ItemCarrito item,
            CatalogProductoResponse producto
    ) {
        if (producto.id() == null
                || !producto.id()
                .equals(item.getProductoId())) {

            throw new OperacionInvalidaException(
                    "El producto del carrito no coincide "
                            + "con Catalog Service"
            );
        }

        if (!Boolean.TRUE.equals(producto.activo())) {
            throw new OperacionInvalidaException(
                    "El producto "
                            + producto.nombre()
                            + " ya no se encuentra activo"
            );
        }

        if (producto.precio() == null
                || producto.precio()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new OperacionInvalidaException(
                    "El producto no posee un precio válido"
            );
        }
    }

    private void validarDatosEntrega(
            CrearPedidoRequest request
    ) {
        if (request.tipoEntrega()
                == TipoEntrega.DESPACHO_DOMICILIO
                && (
                request.direccionEntrega() == null
                        || request.direccionEntrega().isBlank()
        )) {
            throw new OperacionInvalidaException(
                    "La dirección es obligatoria "
                            + "para el despacho a domicilio"
            );
        }
    }

    private String obtenerDireccionNormalizada(
            CrearPedidoRequest request
    ) {
        if (request.tipoEntrega()
                == TipoEntrega.RETIRO_TIENDA) {
            return null;
        }

        return request.direccionEntrega()
                .trim();
    }

    private void validarAuthorizationHeader(
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || authorizationHeader.isBlank()
                || !authorizationHeader.startsWith("Bearer ")) {

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
                    "No fue posible identificar al usuario"
            );
        }

        return email.trim().toLowerCase();
    }

    private PedidoResponse convertirResponse(
            Pedido pedido
    ) {
        List<DetallePedidoResponse> detalles =
                pedido.getDetalles()
                        .stream()
                        .map(this::convertirDetalleResponse)
                        .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getCarritoId(),
                pedido.getClienteEmail(),
                pedido.getSucursalId(),
                pedido.getTipoEntrega(),
                pedido.getDireccionEntrega(),
                pedido.getEstado(),
                pedido.getSubtotal(),
                pedido.getDescuento(),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                detalles
        );
    }

    private DetallePedidoResponse convertirDetalleResponse(
            DetallePedido detalle
    ) {
        return new DetallePedidoResponse(
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
