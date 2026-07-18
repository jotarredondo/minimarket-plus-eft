package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.CrearPedidoRequest;
import com.duoc.minimarket.sales_service.dto.PedidoResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.ItemCarrito;
import com.duoc.minimarket.sales_service.entity.Pedido;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

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
    void debeCrearPedidoDesdeCarrito() {
        Carrito carrito = crearCarritoConProducto();

        when(
                carritoRepository
                        .findByClienteEmailAndEstado(
                                "cliente@minimarket.cl",
                                EstadoCarrito.ACTIVO
                        )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(crearInventario(20));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(crearProducto());

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        PedidoResponse response =
                pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                null
                        ),
                        "Bearer token-cliente"
                );

        assertEquals(
                EstadoPedido.PENDIENTE,
                response.estado()
        );

        assertEquals(
                TipoEntrega.RETIRO_TIENDA,
                response.tipoEntrega()
        );

        assertEquals(1, response.detalles().size());

        assertEquals(
                EstadoCarrito.CONVERTIDO,
                carrito.getEstado()
        );

        verify(pedidoRepository)
                .save(any(Pedido.class));

        verify(carritoRepository)
                .save(carrito);
    }

    @Test
    void debeRechazarCarritoVacio() {
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

        when(
                carritoRepository
                        .findByClienteEmailAndEstado(
                                "cliente@minimarket.cl",
                                EstadoCarrito.ACTIVO
                        )
        ).thenReturn(Optional.of(carrito));

        assertThrows(
                OperacionInvalidaException.class,
                () -> pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.RETIRO_TIENDA,
                                null
                        ),
                        "Bearer token-cliente"
                )
        );
    }

    @Test
    void debeExigirDireccionParaDespacho() {
        assertThrows(
                OperacionInvalidaException.class,
                () -> pedidoService.crearDesdeCarrito(
                        "cliente@minimarket.cl",
                        new CrearPedidoRequest(
                                TipoEntrega.DESPACHO_DOMICILIO,
                                ""
                        ),
                        "Bearer token-cliente"
                )
        );
    }

    private Carrito crearCarritoConProducto() {
        Carrito carrito = Carrito.builder()
                .id(1L)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(1L)
                .estado(EstadoCarrito.ACTIVO)
                .subtotal(new BigDecimal("3980.00"))
                .descuento(BigDecimal.ZERO)
                .total(new BigDecimal("3980.00"))
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
                .cantidad(2)
                .descuento(BigDecimal.ZERO)
                .subtotal(new BigDecimal("3980.00"))
                .build();

        carrito.getItems().add(item);

        return carrito;
    }

    private CatalogProductoResponse crearProducto() {
        return new CatalogProductoResponse(
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida de prueba",
                new BigDecimal("1990.00"),
                true,
                1L,
                "Bebidas"
        );
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
}
