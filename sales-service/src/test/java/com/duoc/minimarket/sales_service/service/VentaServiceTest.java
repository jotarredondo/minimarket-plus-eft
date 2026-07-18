package com.duoc.minimarket.sales_service.service;


import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.VentaResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogSalidaVentaRequest;
import com.duoc.minimarket.sales_service.entity.DetallePedido;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.Pedido;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import com.duoc.minimarket.sales_service.entity.Venta;
import com.duoc.minimarket.sales_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

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
    void debeConfirmarVentaYConservarDetalle() {
        Pedido pedido = crearPedidoPendiente();

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token-cajero"
                )
        ).thenReturn(crearInventario(20));

        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        VentaResponse response =
                ventaService.confirmarVenta(
                        1L,
                        "CAJERO@MINIMARKET.CL",
                        "Bearer token-cajero"
                );

        assertEquals(
                EstadoVenta.CONFIRMADA,
                response.estado()
        );

        assertEquals(
                "cajero@minimarket.cl",
                response.cajeroEmail()
        );

        assertEquals(1, response.detalles().size());

        assertEquals(
                "BEB-001",
                response.detalles().get(0).sku()
        );

        assertEquals(
                2,
                response.detalles().get(0).cantidad()
        );

        assertEquals(
                0,
                response.detalles()
                        .get(0)
                        .precioUnitario()
                        .compareTo(
                                new BigDecimal("1990.00")
                        )
        );

        assertEquals(
                EstadoPedido.COMPLETADO,
                pedido.getEstado()
        );

        verify(catalogClient)
                .registrarSalidaVenta(
                        any(Long.class),
                        any(CatalogSalidaVentaRequest.class),
                        any(String.class)
                );
    }

    @Test
    void debeRechazarPedidoYaVendido() {
        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token-cajero"
                )
        );

        verify(pedidoRepository, never())
                .findById(any(Long.class));
    }

    @Test
    void debeRechazarVentaSinStockSuficiente() {
        Pedido pedido = crearPedidoPendiente();

        when(ventaRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token-cajero"
                )
        ).thenReturn(crearInventario(1));

        assertThrows(
                StockInsuficienteException.class,
                () -> ventaService.confirmarVenta(
                        1L,
                        "cajero@minimarket.cl",
                        "Bearer token-cajero"
                )
        );

        verify(catalogClient, never())
                .registrarSalidaVenta(
                        any(Long.class),
                        any(CatalogSalidaVentaRequest.class),
                        any(String.class)
                );
    }

    private Pedido crearPedidoPendiente() {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .carritoId(1L)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(1L)
                .tipoEntrega(TipoEntrega.RETIRO_TIENDA)
                .estado(EstadoPedido.PENDIENTE)
                .subtotal(new BigDecimal("3980.00"))
                .descuento(BigDecimal.ZERO)
                .total(new BigDecimal("3980.00"))
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
                        .cantidad(2)
                        .descuento(BigDecimal.ZERO)
                        .subtotal(
                                new BigDecimal("3980.00")
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
}
