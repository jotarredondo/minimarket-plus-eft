package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.dto.ReporteRotacionResponse;
import com.duoc.minimarket.sales_service.dto.ResumenVentasResponse;
import com.duoc.minimarket.sales_service.entity.DetalleVenta;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.Venta;
import com.duoc.minimarket.sales_service.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceCoverageTest {

    @Mock
    private VentaRepository ventaRepository;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService =
                new ReporteService(ventaRepository);
    }

    @Test
    void resumenSinVentasDebeRetornarCeros() {
        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of());

        ResumenVentasResponse response =
                reporteService.obtenerResumenVentas();

        assertEquals(0, response.cantidadVentas());

        assertEquals(
                0,
                response.subtotalAcumulado()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                response.descuentosAcumulados()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                response.totalVendido()
                        .compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void rotacionSinVentasDebeRetornarRankingVacio() {
        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of());

        ReporteRotacionResponse response =
                reporteService.obtenerRotacionProductos();

        assertNull(response.productoMasVendido());
        assertNull(response.productoMenosVendido());
        assertEquals(0, response.ranking().size());
    }

    @Test
    void debeIgnorarVentasAnuladas() {
        Venta confirmada = crearVenta(
                1L,
                EstadoVenta.CONFIRMADA,
                1L,
                "BEB-001",
                "Bebida Cola",
                2,
                new BigDecimal("3980.00")
        );

        Venta anulada = crearVenta(
                2L,
                EstadoVenta.ANULADA,
                2L,
                "PAN-001",
                "Pan",
                100,
                new BigDecimal("100000.00")
        );

        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of(anulada, confirmada));

        ResumenVentasResponse resumen =
                reporteService.obtenerResumenVentas();

        ReporteRotacionResponse rotacion =
                reporteService.obtenerRotacionProductos();

        assertEquals(1, resumen.cantidadVentas());

        assertEquals(
                0,
                resumen.totalVendido().compareTo(
                        new BigDecimal("3980.00")
                )
        );

        assertEquals(
                1L,
                rotacion.productoMasVendido()
                        .productoId()
        );
    }

    @Test
    void debeOrdenarProductosPorCantidadVendida() {
        Venta ventaUno = crearVenta(
                1L,
                EstadoVenta.CONFIRMADA,
                1L,
                "BEB-001",
                "Bebida Cola",
                8,
                new BigDecimal("15920.00")
        );

        Venta ventaDos = crearVenta(
                2L,
                EstadoVenta.CONFIRMADA,
                2L,
                "PAN-001",
                "Pan",
                2,
                new BigDecimal("2000.00")
        );

        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of(ventaUno, ventaDos));

        ReporteRotacionResponse response =
                reporteService.obtenerRotacionProductos();

        assertEquals(2, response.ranking().size());

        assertEquals(
                1L,
                response.productoMasVendido()
                        .productoId()
        );

        assertEquals(
                8,
                response.productoMasVendido()
                        .unidadesVendidas()
        );

        assertEquals(
                2L,
                response.productoMenosVendido()
                        .productoId()
        );

        assertEquals(
                2,
                response.productoMenosVendido()
                        .unidadesVendidas()
        );
    }

    private Venta crearVenta(
            Long ventaId,
            EstadoVenta estado,
            Long productoId,
            String sku,
            String nombre,
            int cantidad,
            BigDecimal total
    ) {
        Venta venta = Venta.builder()
                .id(ventaId)
                .pedidoId(ventaId)
                .clienteEmail("cliente@minimarket.cl")
                .cajeroEmail("cajero@minimarket.cl")
                .sucursalId(1L)
                .subtotal(total)
                .descuento(BigDecimal.ZERO)
                .total(total)
                .estado(estado)
                .detalles(new ArrayList<>())
                .build();

        DetalleVenta detalle =
                DetalleVenta.builder()
                        .id(ventaId)
                        .venta(venta)
                        .productoId(productoId)
                        .inventarioId(productoId)
                        .sku(sku)
                        .nombreProducto(nombre)
                        .precioUnitario(
                                total.divide(
                                        BigDecimal.valueOf(cantidad)
                                )
                        )
                        .cantidad(cantidad)
                        .descuento(BigDecimal.ZERO)
                        .subtotal(total)
                        .build();

        venta.getDetalles().add(detalle);

        return venta;
    }
}
