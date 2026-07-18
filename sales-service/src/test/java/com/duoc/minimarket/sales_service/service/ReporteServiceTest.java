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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService =
                new ReporteService(ventaRepository);
    }

    @Test
    void debeCalcularResumenDeVentas() {
        Venta venta = crearVentaConfirmada();

        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of(venta));

        ResumenVentasResponse response =
                reporteService.obtenerResumenVentas();

        assertEquals(1, response.cantidadVentas());

        assertEquals(
                0,
                response.totalVendido().compareTo(
                        new BigDecimal("3582.00")
                )
        );

        assertEquals(
                0,
                response.descuentosAcumulados()
                        .compareTo(
                                new BigDecimal("398.00")
                        )
        );
    }

    @Test
    void debeIdentificarProductoMasVendido() {
        Venta venta = crearVentaConfirmada();

        when(
                ventaRepository
                        .findAllByOrderByFechaVentaDesc()
        ).thenReturn(List.of(venta));

        ReporteRotacionResponse response =
                reporteService.obtenerRotacionProductos();

        assertEquals(
                1L,
                response.productoMasVendido()
                        .productoId()
        );

        assertEquals(
                2,
                response.productoMasVendido()
                        .unidadesVendidas()
        );

        assertEquals(1, response.ranking().size());
    }

    private Venta crearVentaConfirmada() {
        Venta venta = Venta.builder()
                .id(1L)
                .pedidoId(1L)
                .clienteEmail("cliente@minimarket.cl")
                .cajeroEmail("cajero@minimarket.cl")
                .sucursalId(1L)
                .subtotal(new BigDecimal("3980.00"))
                .descuento(new BigDecimal("398.00"))
                .total(new BigDecimal("3582.00"))
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
                        .cantidad(2)
                        .descuento(
                                new BigDecimal("398.00")
                        )
                        .subtotal(
                                new BigDecimal("3582.00")
                        )
                        .build();

        venta.getDetalles().add(detalle);

        return venta;
    }
}
