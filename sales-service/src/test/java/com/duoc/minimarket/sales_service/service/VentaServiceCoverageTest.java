package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.VentaResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogSalidaVentaRequest;
import com.duoc.minimarket.sales_service.entity.DetallePedido;
import com.duoc.minimarket.sales_service.entity.DetalleVenta;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.Pedido;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import com.duoc.minimarket.sales_service.entity.Venta;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.repository.PedidoRepository;
import com.duoc.minimarket.sales_service.repository.VentaRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceCoverageTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private CatalogClient catalogClient;

    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        ventaService = new VentaService(
                pedidoRepository,
                ventaRepository,
                catalogClient
        );
    }

    @Test
    void debeObtenerVentaPorId() {
        Venta venta = crearVenta(1L);

        when(ventaRepository.findById(1L))
                .thenReturn(Optional.of(venta));

        VentaResponse response =
                ventaService.obtenerPorId(1L);

        assertEquals(1L, response.id());
        assertEquals(EstadoVenta.CONFIRMADA, response.estado());
        assertEquals(1, response.detalles().size());
    }

    @Test
    void debeInformarVentaNoEncontrada() {
        when(ventaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> ventaService.obtenerPorId(99L)
        );
    }

    @Test
    void debeListarTodasLasVentas() {
        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(
                List.of(
                        crearVenta(2L),
                        crearVenta(1L)
                )
        );

        List<VentaResponse> responses =
                ventaService.listarTodas();

        assertEquals(2, responses.size());
    }

    @Test
    void debeListarVentasPorCajeroNormalizandoEmail() {
        when(
                ventaRepository
                        .findByCajeroEmailOrderByFechaVentaDesc(
                                "cajero@minimarket.cl"
                        )
        ).thenReturn(List.of(crearVenta(1L)));

        List<VentaResponse> responses =
                ventaService.listarPorCajero(
                        " CAJERO@MINIMARKET.CL "
                );

        assertEquals(1, responses.size());

        verify(ventaRepository)
                .findByCajeroEmailOrderByFechaVentaDesc(
                        "cajero@minimarket.cl"
                );
    }

    @Test
    void debeRechazarHeaderInvalido() {
        assertThrows(
                OperacionInvalidaException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        null
                )
        );

        verify(ventaRepository, never())
                .existsByPedidoId(any());
    }

    @Test
    void debeInformarPedidoNoEncontrado() {
        when(ventaRepository.existsByPedidoId(99L))
                .thenReturn(false);

        when(pedidoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> ventaService.confirmarVenta(
                        99L,
                        "cajero@minimarket.cl",
                        "Bearer token"
                )
        );
    }

    @Test
    void debeRechazarPedidoNoPendiente() {
        Pedido pedido =
                crearPedido(EstadoPedido.COMPLETADO, true);

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        assertThrows(
                OperacionInvalidaException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token"
                )
        );

        verify(catalogClient, never())
                .obtenerInventario(any(), any());
    }

    @Test
    void debeRechazarPedidoSinDetalles() {
        Pedido pedido =
                crearPedido(EstadoPedido.PENDIENTE, false);

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        assertThrows(
                OperacionInvalidaException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token"
                )
        );
    }

    @Test
    void debeRechazarInventarioConProductoDistinto() {
        Pedido pedido =
                crearPedido(EstadoPedido.PENDIENTE, true);

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(
                crearInventario(
                        99L,
                        1L,
                        20
                )
        );

        assertThrows(
                OperacionInvalidaException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token"
                )
        );

        verify(catalogClient, never())
                .registrarSalidaVenta(
                        any(),
                        any(CatalogSalidaVentaRequest.class),
                        any()
                );
    }

    @Test
    void debeRechazarInventarioDeOtraSucursal() {
        Pedido pedido =
                crearPedido(EstadoPedido.PENDIENTE, true);

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(
                crearInventario(
                        1L,
                        99L,
                        20
                )
        );

        assertThrows(
                OperacionInvalidaException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token"
                )
        );
    }

    private Pedido crearPedido(
            EstadoPedido estado,
            boolean conDetalle
    ) {
        Pedido pedido = Pedido.builder()
                .id(1L)
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

        if (conDetalle) {
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
        }

        return pedido;
    }

    private Venta crearVenta(Long id) {
        Venta venta = Venta.builder()
                .id(id)
                .pedidoId(id)
                .clienteEmail("cliente@minimarket.cl")
                .cajeroEmail("cajero@minimarket.cl")
                .sucursalId(1L)
                .subtotal(new BigDecimal("1990.00"))
                .descuento(BigDecimal.ZERO)
                .total(new BigDecimal("1990.00"))
                .estado(EstadoVenta.CONFIRMADA)
                .detalles(new ArrayList<>())
                .build();

        DetalleVenta detalle =
                DetalleVenta.builder()
                        .id(1L)
                        .venta(venta)
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

        venta.getDetalles().add(detalle);

        return venta;
    }

    private CatalogInventarioResponse crearInventario(
            Long productoId,
            Long sucursalId,
            Integer stock
    ) {
        return new CatalogInventarioResponse(
                1L,
                productoId,
                "BEB-001",
                "Bebida Cola 1.5 L",
                sucursalId,
                "SUC-001",
                "Sucursal Centro",
                stock,
                10,
                false,
                LocalDateTime.now()
        );
    }
}
