package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.CrearPedidoRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceCoverageTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CatalogClient catalogClient;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                carritoRepository,
                pedidoRepository,
                catalogClient
        );
    }

    @Test
    void debeCrearPedidoConDespachoYDomicilio() {
        Carrito carrito = crearCarritoConProducto(2);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(20));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearProducto(true));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PedidoResponse response =
                pedidoService.crearDesdeCarrito(
                        "CLIENTE@MINIMARKET.CL",
                        new CrearPedidoRequest(
                                TipoEntrega.DESPACHO_DOMICILIO,
                                "  Avenida Principal 123  "
                        ),
                        "Bearer token"
                );

        assertEquals(
                TipoEntrega.DESPACHO_DOMICILIO,
                response.tipoEntrega()
        );

        assertEquals(
                "Avenida Principal 123",
                response.direccionEntrega()
        );

        assertEquals(
                EstadoCarrito.CONVERTIDO,
                carrito.getEstado()
        );
    }

    @Test
    void pedidoParaRetiroDebeQuedarSinDireccion() {
        Carrito carrito = crearCarritoConProducto(1);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(20));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearProducto(true));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PedidoResponse response =
                pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                "Dirección ignorada"
                        ),
                        "Bearer token"
                );

        assertNull(response.direccionEntrega());
    }

    @Test
    void debeListarPedidosDelCliente() {
        Pedido pedidoUno =
                crearPedido(1L, EstadoPedido.PENDIENTE);

        Pedido pedidoDos =
                crearPedido(2L, EstadoPedido.COMPLETADO);

        when(
                pedidoRepository
                        .findByClienteEmailOrderByFechaCreacionDesc(
                                "cliente@minimarket.cl"
                        )
        ).thenReturn(List.of(pedidoDos, pedidoUno));

        List<PedidoResponse> responses =
                pedidoService.listarPedidosCliente(
                        "CLIENTE@MINIMARKET.CL"
                );

        assertEquals(2, responses.size());
        assertEquals(
                EstadoPedido.COMPLETADO,
                responses.get(0).estado()
        );
    }

    @Test
    void debeObtenerPedidoDelCliente() {
        Pedido pedido =
                crearPedido(1L, EstadoPedido.PENDIENTE);

        when(
                pedidoRepository.findByIdAndClienteEmail(
                        1L,
                        "cliente@minimarket.cl"
                )
        ).thenReturn(Optional.of(pedido));

        PedidoResponse response =
                pedidoService.obtenerPedidoCliente(
                        1L,
                        "cliente@minimarket.cl"
                );

        assertEquals(1L, response.id());
        assertEquals(
                "cliente@minimarket.cl",
                response.clienteEmail()
        );
    }

    @Test
    void debeInformarPedidoClienteNoEncontrado() {
        when(
                pedidoRepository.findByIdAndClienteEmail(
                        99L,
                        "cliente@minimarket.cl"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> pedidoService.obtenerPedidoCliente(
                        99L,
                        "cliente@minimarket.cl"
                )
        );
    }

    @Test
    void debeListarPedidosPendientes() {
        Pedido pedido =
                crearPedido(1L, EstadoPedido.PENDIENTE);

        when(
                pedidoRepository
                        .findByEstadoOrderByFechaCreacionAsc(
                                EstadoPedido.PENDIENTE
                        )
        ).thenReturn(List.of(pedido));

        List<PedidoResponse> responses =
                pedidoService.listarPendientes();

        assertEquals(1, responses.size());
        assertEquals(
                EstadoPedido.PENDIENTE,
                responses.get(0).estado()
        );
    }

    @Test
    void debeObtenerPedidoParaGestion() {
        Pedido pedido =
                crearPedido(1L, EstadoPedido.PENDIENTE);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        PedidoResponse response =
                pedidoService.obtenerPorIdGestion(1L);

        assertEquals(1L, response.id());
    }

    @Test
    void debeInformarPedidoGestionNoEncontrado() {
        when(pedidoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> pedidoService
                        .obtenerPorIdGestion(99L)
        );
    }

    @Test
    void debeRechazarPedidoPorStockInsuficiente() {
        Carrito carrito = crearCarritoConProducto(5);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(2));

        assertThrows(
                StockInsuficienteException.class,
                () -> pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                null
                        ),
                        "Bearer token"
                )
        );

        verify(catalogClient, never())
                .obtenerProducto(any(), any());
    }

    @Test
    void debeRechazarProductoInactivoAlCrearPedido() {
        Carrito carrito = crearCarritoConProducto(2);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(20));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearProducto(false));

        assertThrows(
                OperacionInvalidaException.class,
                () -> pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                null
                        ),
                        "Bearer token"
                )
        );
    }

    @Test
    void debeRechazarHeaderInvalido() {
        assertThrows(
                OperacionInvalidaException.class,
                () -> pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                null
                        ),
                        ""
                )
        );

        verify(carritoRepository, never())
                .findByClienteEmailAndEstado(
                        any(),
                        any()
                );
    }

    private Carrito crearCarritoConProducto(
            int cantidad
    ) {
        Carrito carrito = Carrito.builder()
                .id(1L)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(1L)
                .estado(EstadoCarrito.ACTIVO)
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        ItemCarrito item = ItemCarrito.builder()
                .id(1L)
                .carrito(carrito)
                .productoId(1L)
                .inventarioId(1L)
                .sku("BEB-001")
                .nombreProducto("Bebida Cola 1.5 L")
                .precioUnitario(new BigDecimal("1990.00"))
                .cantidad(cantidad)
                .descuento(BigDecimal.ZERO)
                .subtotal(
                        new BigDecimal("1990.00")
                                .multiply(
                                        BigDecimal.valueOf(cantidad)
                                )
                )
                .build();

        carrito.getItems().add(item);
        carrito.recalcularTotales();

        return carrito;
    }

    private Pedido crearPedido(
            Long id,
            EstadoPedido estado
    ) {
        Pedido pedido = Pedido.builder()
                .id(id)
                .carritoId(1L)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(1L)
                .tipoEntrega(TipoEntrega.RETIRO_TIENDA)
                .estado(estado)
                .subtotal(new BigDecimal("1990.00"))
                .descuento(BigDecimal.ZERO)
                .total(new BigDecimal("1990.00"))
                .detalles(new ArrayList<>())
                .build();

        DetallePedido detalle =
                DetallePedido.builder()
                        .id(1L)
                        .pedido(pedido)
                        .productoId(1L)
                        .inventarioId(1L)
                        .sku("BEB-001")
                        .nombreProducto(
                                "Bebida Cola 1.5 L"
                        )
                        .precioUnitario(
                                new BigDecimal("1990.00")
                        )
                        .cantidad(1)
                        .descuento(BigDecimal.ZERO)
                        .subtotal(
                                new BigDecimal("1990.00")
                        )
                        .build();

        pedido.getDetalles().add(detalle);

        return pedido;
    }

    private CatalogInventarioResponse crearInventario(
            Integer stock
    ) {
        return new CatalogInventarioResponse(
                1L,
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                1L,
                "SUC-001",
                "Sucursal Centro",
                stock,
                10,
                false,
                LocalDateTime.now()
        );
    }

    private CatalogProductoResponse crearProducto(
            boolean activo
    ) {
        return new CatalogProductoResponse(
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida de prueba",
                new BigDecimal("1990.00"),
                activo,
                1L,
                "Bebidas"
        );
    }
}
