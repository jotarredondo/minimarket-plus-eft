package com.duoc.minimarket.sales_service.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityCoverageTest {

    @Test
    void itemCarritoDebeCalcularSubtotalConDescuento() {
        ItemCarrito item =
                ItemCarrito.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .sku("BEB-001")
                        .nombreProducto("Bebida Cola")
                        .precioUnitario(
                                new BigDecimal("1000.00")
                        )
                        .cantidad(2)
                        .descuento(
                                new BigDecimal("200.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        item.recalcularSubtotal();

        assertEquals(
                0,
                item.getSubtotal().compareTo(
                        new BigDecimal("1800.00")
                )
        );

        assertEquals(
                0,
                item.getDescuentoSeguro().compareTo(
                        new BigDecimal("200.00")
                )
        );
    }

    @Test
    void itemCarritoDebeManejarDescuentoNulo() {
        ItemCarrito item =
                ItemCarrito.builder()
                        .precioUnitario(
                                new BigDecimal("1000.00")
                        )
                        .cantidad(2)
                        .descuento(null)
                        .subtotal(BigDecimal.ZERO)
                        .build();

        item.recalcularSubtotal();

        assertEquals(
                0,
                item.getDescuentoSeguro()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                item.getSubtotal().compareTo(
                        new BigDecimal("2000.00")
                )
        );
    }

    @Test
    void carritoDebeAgregarItemYCalcularTotales() {
        Carrito carrito =
                Carrito.builder()
                        .clienteEmail(
                                "cliente@minimarket.cl"
                        )
                        .sucursalId(1L)
                        .estado(EstadoCarrito.ACTIVO)
                        .subtotal(BigDecimal.ZERO)
                        .descuento(BigDecimal.ZERO)
                        .total(BigDecimal.ZERO)
                        .items(new ArrayList<>())
                        .build();

        ItemCarrito item =
                ItemCarrito.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .precioUnitario(
                                new BigDecimal("1000.00")
                        )
                        .cantidad(2)
                        .descuento(
                                new BigDecimal("200.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        item.recalcularSubtotal();
        carrito.agregarItem(item);
        carrito.recalcularTotales();

        assertEquals(1, carrito.getItems().size());
        assertSame(carrito, item.getCarrito());

        assertEquals(
                0,
                carrito.getSubtotal().compareTo(
                        new BigDecimal("2000.00")
                )
        );

        assertEquals(
                0,
                carrito.getDescuento().compareTo(
                        new BigDecimal("200.00")
                )
        );

        assertEquals(
                0,
                carrito.getTotal().compareTo(
                        new BigDecimal("1800.00")
                )
        );
    }

    @Test
    void detallePedidoDebeCalcularSubtotal() {
        DetallePedido detalle =
                DetallePedido.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .precioUnitario(
                                new BigDecimal("1000.00")
                        )
                        .cantidad(3)
                        .descuento(
                                new BigDecimal("300.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        detalle.recalcularSubtotal();

        assertEquals(
                0,
                detalle.getSubtotal().compareTo(
                        new BigDecimal("2700.00")
                )
        );

        assertEquals(
                0,
                detalle.getDescuentoSeguro().compareTo(
                        new BigDecimal("300.00")
                )
        );
    }

    @Test
    void pedidoDebeAgregarDetalleYCalcularTotales() {
        Pedido pedido =
                Pedido.builder()
                        .carritoId(1L)
                        .clienteEmail(
                                "cliente@minimarket.cl"
                        )
                        .sucursalId(1L)
                        .tipoEntrega(
                                TipoEntrega.RETIRO_TIENDA
                        )
                        .estado(EstadoPedido.PENDIENTE)
                        .subtotal(BigDecimal.ZERO)
                        .descuento(BigDecimal.ZERO)
                        .total(BigDecimal.ZERO)
                        .detalles(new ArrayList<>())
                        .build();

        DetallePedido detalle =
                DetallePedido.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .precioUnitario(
                                new BigDecimal("1000.00")
                        )
                        .cantidad(3)
                        .descuento(
                                new BigDecimal("300.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        pedido.agregarDetalle(detalle);

        assertEquals(1, pedido.getDetalles().size());
        assertSame(pedido, detalle.getPedido());

        assertEquals(
                0,
                pedido.getSubtotal().compareTo(
                        new BigDecimal("3000.00")
                )
        );

        assertEquals(
                0,
                pedido.getDescuento().compareTo(
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                0,
                pedido.getTotal().compareTo(
                        new BigDecimal("2700.00")
                )
        );
    }

    @Test
    void detalleVentaDebeCalcularSubtotal() {
        DetalleVenta detalle =
                DetalleVenta.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .precioUnitario(
                                new BigDecimal("2000.00")
                        )
                        .cantidad(2)
                        .descuento(
                                new BigDecimal("400.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        detalle.recalcularSubtotal();

        assertEquals(
                0,
                detalle.getSubtotal().compareTo(
                        new BigDecimal("3600.00")
                )
        );

        assertEquals(
                0,
                detalle.getDescuentoSeguro().compareTo(
                        new BigDecimal("400.00")
                )
        );
    }

    @Test
    void ventaDebeAgregarDetalleYCalcularTotales() {
        Venta venta =
                Venta.builder()
                        .pedidoId(1L)
                        .clienteEmail(
                                "cliente@minimarket.cl"
                        )
                        .cajeroEmail(
                                "cajero@minimarket.cl"
                        )
                        .sucursalId(1L)
                        .estado(EstadoVenta.CONFIRMADA)
                        .subtotal(BigDecimal.ZERO)
                        .descuento(BigDecimal.ZERO)
                        .total(BigDecimal.ZERO)
                        .detalles(new ArrayList<>())
                        .build();

        DetalleVenta detalle =
                DetalleVenta.builder()
                        .productoId(1L)
                        .inventarioId(1L)
                        .precioUnitario(
                                new BigDecimal("2000.00")
                        )
                        .cantidad(2)
                        .descuento(
                                new BigDecimal("400.00")
                        )
                        .subtotal(BigDecimal.ZERO)
                        .build();

        venta.agregarDetalle(detalle);

        assertEquals(1, venta.getDetalles().size());
        assertSame(venta, detalle.getVenta());

        assertEquals(
                0,
                venta.getSubtotal().compareTo(
                        new BigDecimal("4000.00")
                )
        );

        assertEquals(
                0,
                venta.getDescuento().compareTo(
                        new BigDecimal("400.00")
                )
        );

        assertEquals(
                0,
                venta.getTotal().compareTo(
                        new BigDecimal("3600.00")
                )
        );
    }

    @Test
    void promocionPorcentajeDebeCalcularDescuento() {
        LocalDateTime ahora =
                LocalDateTime.now();

        Promocion promocion =
                Promocion.builder()
                        .nombre("Descuento 10%")
                        .productoId(1L)
                        .tipo(
                                TipoPromocion.PORCENTAJE
                        )
                        .valor(new BigDecimal("10"))
                        .fechaInicio(
                                ahora.minusDays(1)
                        )
                        .fechaFin(
                                ahora.plusDays(1)
                        )
                        .activo(true)
                        .build();

        assertTrue(promocion.esVigente(ahora));

        BigDecimal descuento =
                promocion.calcularDescuento(
                        new BigDecimal("1000.00"),
                        2,
                        ahora
                );

        assertEquals(
                0,
                descuento.compareTo(
                        new BigDecimal("200.00")
                )
        );
    }

    @Test
    void promocionMontoFijoDebeCalcularDescuento() {
        LocalDateTime ahora =
                LocalDateTime.now();

        Promocion promocion =
                Promocion.builder()
                        .nombre("Descuento fijo")
                        .productoId(1L)
                        .tipo(
                                TipoPromocion.MONTO_FIJO
                        )
                        .valor(
                                new BigDecimal("300.00")
                        )
                        .fechaInicio(
                                ahora.minusDays(1)
                        )
                        .fechaFin(
                                ahora.plusDays(1)
                        )
                        .activo(true)
                        .build();

        BigDecimal descuento =
                promocion.calcularDescuento(
                        new BigDecimal("1000.00"),
                        2,
                        ahora
                );

        assertTrue(
                descuento.compareTo(BigDecimal.ZERO) > 0
        );

        assertTrue(
                descuento.compareTo(
                        new BigDecimal("2000.00")
                ) <= 0
        );
    }

    @Test
    void promocionExpiradaNoDebeAplicarse() {
        LocalDateTime ahora =
                LocalDateTime.now();

        Promocion promocion =
                Promocion.builder()
                        .nombre("Promoción expirada")
                        .productoId(1L)
                        .tipo(
                                TipoPromocion.PORCENTAJE
                        )
                        .valor(new BigDecimal("20"))
                        .fechaInicio(
                                ahora.minusDays(10)
                        )
                        .fechaFin(
                                ahora.minusDays(1)
                        )
                        .activo(true)
                        .build();

        assertFalse(promocion.esVigente(ahora));

        BigDecimal descuento =
                promocion.calcularDescuento(
                        new BigDecimal("1000.00"),
                        2,
                        ahora
                );

        assertEquals(
                0,
                descuento.compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void promocionInactivaNoDebeAplicarse() {
        LocalDateTime ahora =
                LocalDateTime.now();

        Promocion promocion =
                Promocion.builder()
                        .nombre("Promoción inactiva")
                        .productoId(1L)
                        .tipo(
                                TipoPromocion.PORCENTAJE
                        )
                        .valor(new BigDecimal("20"))
                        .fechaInicio(
                                ahora.minusDays(1)
                        )
                        .fechaFin(
                                ahora.plusDays(1)
                        )
                        .activo(false)
                        .build();

        assertFalse(promocion.esVigente(ahora));

        assertEquals(
                0,
                promocion.calcularDescuento(
                                new BigDecimal("1000.00"),
                                2,
                                ahora
                        )
                        .compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void promocionDebeInicializarEstado() {
        Promocion promocion =
                Promocion.builder()
                        .nombre("Promoción nueva")
                        .productoId(1L)
                        .tipo(
                                TipoPromocion.PORCENTAJE
                        )
                        .valor(new BigDecimal("10"))
                        .fechaInicio(
                                LocalDateTime.now()
                        )
                        .fechaFin(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .activo(null)
                        .build();

        promocion.antesDeCrear();

        assertNotNull(promocion.getActivo());
        assertTrue(promocion.getActivo());
    }
}