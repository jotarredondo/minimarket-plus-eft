package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.dto.ProductoRotacionResponse;
import com.duoc.minimarket.sales_service.dto.ReporteRotacionResponse;
import com.duoc.minimarket.sales_service.dto.ResumenVentasResponse;
import com.duoc.minimarket.sales_service.entity.DetalleVenta;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.Venta;
import com.duoc.minimarket.sales_service.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final VentaRepository ventaRepository;

    public ReporteService(
            VentaRepository ventaRepository
    ) {
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public ResumenVentasResponse obtenerResumenVentas() {
        List<Venta> ventasConfirmadas =
                obtenerVentasConfirmadas();

        BigDecimal subtotal = ventasConfirmadas.stream()
                .map(Venta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuentos = ventasConfirmadas.stream()
                .map(Venta::getDescuento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = ventasConfirmadas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenVentasResponse(
                ventasConfirmadas.size(),
                subtotal,
                descuentos,
                total
        );
    }

    @Transactional(readOnly = true)
    public ReporteRotacionResponse obtenerRotacionProductos() {
        Map<Long, ProductoAcumulado> acumulados =
                new LinkedHashMap<>();

        for (Venta venta : obtenerVentasConfirmadas()) {
            for (DetalleVenta detalle
                    : venta.getDetalles()) {

                ProductoAcumulado acumulado =
                        acumulados.computeIfAbsent(
                                detalle.getProductoId(),
                                productoId ->
                                        new ProductoAcumulado(
                                                productoId,
                                                detalle.getSku(),
                                                detalle.getNombreProducto()
                                        )
                        );

                acumulado.agregar(detalle);
            }
        }

        List<ProductoRotacionResponse> ranking =
                new ArrayList<>();

        for (ProductoAcumulado acumulado
                : acumulados.values()) {

            ranking.add(acumulado.convertir());
        }

        ranking.sort(
                Comparator.comparingLong(
                                ProductoRotacionResponse
                                        ::unidadesVendidas
                        )
                        .reversed()
        );

        ProductoRotacionResponse masVendido =
                ranking.isEmpty()
                        ? null
                        : ranking.get(0);

        ProductoRotacionResponse menosVendido =
                ranking.isEmpty()
                        ? null
                        : ranking.get(
                        ranking.size() - 1
                );

        return new ReporteRotacionResponse(
                masVendido,
                menosVendido,
                ranking
        );
    }

    private List<Venta> obtenerVentasConfirmadas() {
        return ventaRepository
                .findAllByOrderByFechaVentaDesc()
                .stream()
                .filter(venta ->
                        venta.getEstado()
                                == EstadoVenta.CONFIRMADA
                )
                .toList();
    }

    private static class ProductoAcumulado {

        private final Long productoId;
        private final String sku;
        private final String nombre;
        private long unidades;
        private BigDecimal total;

        private ProductoAcumulado(
                Long productoId,
                String sku,
                String nombre
        ) {
            this.productoId = productoId;
            this.sku = sku;
            this.nombre = nombre;
            this.unidades = 0;
            this.total = BigDecimal.ZERO;
        }

        private void agregar(
                DetalleVenta detalle
        ) {
            unidades += detalle.getCantidad();

            total = total.add(
                    detalle.getSubtotal()
            );
        }

        private ProductoRotacionResponse convertir() {
            return new ProductoRotacionResponse(
                    productoId,
                    sku,
                    nombre,
                    unidades,
                    total
            );
        }
    }
}