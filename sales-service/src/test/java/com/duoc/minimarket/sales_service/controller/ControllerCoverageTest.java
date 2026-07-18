package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.CrearPedidoRequest;
import com.duoc.minimarket.sales_service.dto.PedidoResponse;
import com.duoc.minimarket.sales_service.dto.PromocionRequest;
import com.duoc.minimarket.sales_service.dto.PromocionResponse;
import com.duoc.minimarket.sales_service.dto.ReporteRotacionResponse;
import com.duoc.minimarket.sales_service.dto.ResumenVentasResponse;
import com.duoc.minimarket.sales_service.dto.VentaResponse;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import com.duoc.minimarket.sales_service.entity.EstadoVenta;
import com.duoc.minimarket.sales_service.entity.TipoEntrega;
import com.duoc.minimarket.sales_service.entity.TipoPromocion;
import com.duoc.minimarket.sales_service.service.PedidoService;
import com.duoc.minimarket.sales_service.service.PromocionService;
import com.duoc.minimarket.sales_service.service.ReporteService;
import com.duoc.minimarket.sales_service.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerCoverageTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private VentaService ventaService;

    @Mock
    private PromocionService promocionService;

    @Mock
    private ReporteService reporteService;

    private Authentication authentication;

    private PedidoController pedidoController;
    private VentaController ventaController;
    private PromocionController promocionController;
    private ReporteController reporteController;

    @BeforeEach
    void setUp() {
        pedidoController =
                new PedidoController(pedidoService);

        ventaController =
                new VentaController(ventaService);

        promocionController =
                new PromocionController(promocionService);

        reporteController =
                new ReporteController(reporteService);

        authentication =
                new UsernamePasswordAuthenticationToken(
                        "usuario@minimarket.cl",
                        null,
                        List.of()
                );
    }

    @Test
    void debeCubrirCreacionDePedido() {
        CrearPedidoRequest request =
                new CrearPedidoRequest(
                        TipoEntrega.RETIRO_TIENDA,
                        null
                );

        PedidoResponse responseEsperado =
                crearPedidoResponse();

        when(
                pedidoService.crearDesdeCarrito(
                        "usuario@minimarket.cl",
                        request,
                        "Bearer token"
                )
        ).thenReturn(responseEsperado);

        ResponseEntity<PedidoResponse> response =
                pedidoController.crear(
                        request,
                        authentication,
                        "Bearer token"
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                EstadoPedido.PENDIENTE,
                response.getBody().estado()
        );

        verify(pedidoService)
                .crearDesdeCarrito(
                        "usuario@minimarket.cl",
                        request,
                        "Bearer token"
                );
    }

    @Test
    void debeCubrirConsultasDePedidos() {
        PedidoResponse pedido =
                crearPedidoResponse();

        when(
                pedidoService.listarPedidosCliente(
                        "usuario@minimarket.cl"
                )
        ).thenReturn(List.of(pedido));

        when(
                pedidoService.obtenerPedidoCliente(
                        1L,
                        "usuario@minimarket.cl"
                )
        ).thenReturn(pedido);

        when(pedidoService.listarPendientes())
                .thenReturn(List.of(pedido));

        when(pedidoService.obtenerPorIdGestion(1L))
                .thenReturn(pedido);

        ResponseEntity<List<PedidoResponse>>
                pedidosCliente =
                pedidoController.listarPedidosCliente(
                        authentication
                );

        ResponseEntity<PedidoResponse>
                pedidoCliente =
                pedidoController.obtenerPedidoCliente(
                        1L,
                        authentication
                );

        ResponseEntity<List<PedidoResponse>>
                pendientes =
                pedidoController.listarPendientes();

        ResponseEntity<PedidoResponse>
                gestion =
                pedidoController.obtenerParaGestion(1L);

        assertEquals(
                HttpStatus.OK,
                pedidosCliente.getStatusCode()
        );

        assertNotNull(pedidosCliente.getBody());

        assertEquals(
                1,
                pedidosCliente.getBody().size()
        );

        assertNotNull(pedidoCliente.getBody());

        assertEquals(
                1L,
                pedidoCliente.getBody().id()
        );

        assertNotNull(pendientes.getBody());

        assertEquals(
                1,
                pendientes.getBody().size()
        );

        assertNotNull(gestion.getBody());

        assertEquals(
                1L,
                gestion.getBody().id()
        );
    }

    @Test
    void debeCubrirConfirmacionDeVenta() {
        VentaResponse venta =
                crearVentaResponse();

        when(
                ventaService.confirmarVenta(
                        1L,
                        "usuario@minimarket.cl",
                        "Bearer token"
                )
        ).thenReturn(venta);

        ResponseEntity<VentaResponse> response =
                ventaController.confirmarVenta(
                        1L,
                        authentication,
                        "Bearer token"
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                EstadoVenta.CONFIRMADA,
                response.getBody().estado()
        );

        verify(ventaService)
                .confirmarVenta(
                        1L,
                        "usuario@minimarket.cl",
                        "Bearer token"
                );
    }

    @Test
    void debeCubrirConsultasDeVentas() {
        VentaResponse venta =
                crearVentaResponse();

        when(
                ventaService.listarPorCajero(
                        "usuario@minimarket.cl"
                )
        ).thenReturn(List.of(venta));

        when(ventaService.listarTodas())
                .thenReturn(List.of(venta));

        when(ventaService.obtenerPorId(1L))
                .thenReturn(venta);

        ResponseEntity<List<VentaResponse>>
                ventasCajero =
                ventaController.listarMisVentas(
                        authentication
                );

        ResponseEntity<List<VentaResponse>>
                todasLasVentas =
                ventaController.listarTodas();

        ResponseEntity<VentaResponse>
                ventaPorId =
                ventaController.obtenerPorId(1L);

        assertEquals(
                HttpStatus.OK,
                ventasCajero.getStatusCode()
        );

        assertNotNull(ventasCajero.getBody());

        assertEquals(
                1,
                ventasCajero.getBody().size()
        );

        assertNotNull(todasLasVentas.getBody());

        assertEquals(
                1,
                todasLasVentas.getBody().size()
        );

        assertNotNull(ventaPorId.getBody());

        assertEquals(
                1L,
                ventaPorId.getBody().id()
        );
    }

    @Test
    void debeCubrirCreacionDePromocion() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(30);

        PromocionRequest request =
                new PromocionRequest(
                        "Descuento bebidas",
                        "Promoción de prueba",
                        1L,
                        TipoPromocion.PORCENTAJE,
                        new BigDecimal("10"),
                        inicio,
                        fin
                );

        PromocionResponse promocion =
                crearPromocionResponse(
                        inicio,
                        fin
                );

        when(
                promocionService.crear(
                        request,
                        "Bearer token"
                )
        ).thenReturn(promocion);

        ResponseEntity<PromocionResponse> response =
                promocionController.crear(
                        request,
                        "Bearer token"
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1L,
                response.getBody().id()
        );
    }

    @Test
    void debeCubrirConsultasYCambioDePromocion() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(30);

        PromocionResponse activa =
                crearPromocionResponse(
                        inicio,
                        fin
                );

        PromocionResponse inactiva =
                new PromocionResponse(
                        activa.id(),
                        activa.nombre(),
                        activa.descripcion(),
                        activa.productoId(),
                        activa.tipo(),
                        activa.valor(),
                        activa.fechaInicio(),
                        activa.fechaFin(),
                        false
                );

        when(promocionService.listarActivas())
                .thenReturn(List.of(activa));

        when(
                promocionService.cambiarEstado(
                        1L,
                        false
                )
        ).thenReturn(inactiva);

        ResponseEntity<List<PromocionResponse>>
                listado =
                promocionController.listarActivas();

        ResponseEntity<PromocionResponse>
                cambioEstado =
                promocionController.cambiarEstado(
                        1L,
                        false
                );

        assertEquals(
                HttpStatus.OK,
                listado.getStatusCode()
        );

        assertNotNull(listado.getBody());

        assertEquals(
                1,
                listado.getBody().size()
        );

        assertNotNull(cambioEstado.getBody());

        assertEquals(
                false,
                cambioEstado.getBody().activo()
        );
    }

    @Test
    void debeCubrirEndpointsDeReportes() {
        ResumenVentasResponse resumen =
                new ResumenVentasResponse(
                        2,
                        new BigDecimal("5000.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("4500.00")
                );

        ReporteRotacionResponse rotacion =
                new ReporteRotacionResponse(
                        null,
                        null,
                        List.of()
                );

        when(reporteService.obtenerResumenVentas())
                .thenReturn(resumen);

        when(
                reporteService.obtenerRotacionProductos()
        ).thenReturn(rotacion);

        ResponseEntity<ResumenVentasResponse>
                resumenResponse =
                reporteController.obtenerResumenVentas();

        ResponseEntity<ReporteRotacionResponse>
                rotacionResponse =
                reporteController
                        .obtenerRotacionProductos();

        assertEquals(
                HttpStatus.OK,
                resumenResponse.getStatusCode()
        );

        assertNotNull(resumenResponse.getBody());

        assertEquals(
                2,
                resumenResponse.getBody()
                        .cantidadVentas()
        );

        assertEquals(
                HttpStatus.OK,
                rotacionResponse.getStatusCode()
        );

        assertNotNull(rotacionResponse.getBody());

        assertEquals(
                0,
                rotacionResponse.getBody()
                        .ranking()
                        .size()
        );
    }

    private PedidoResponse crearPedidoResponse() {
        return new PedidoResponse(
                1L,
                1L,
                "cliente@minimarket.cl",
                1L,
                TipoEntrega.RETIRO_TIENDA,
                null,
                EstadoPedido.PENDIENTE,
                new BigDecimal("1990.00"),
                BigDecimal.ZERO,
                new BigDecimal("1990.00"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
    }

    private VentaResponse crearVentaResponse() {
        return new VentaResponse(
                1L,
                1L,
                "cliente@minimarket.cl",
                "cajero@minimarket.cl",
                1L,
                new BigDecimal("1990.00"),
                BigDecimal.ZERO,
                new BigDecimal("1990.00"),
                EstadoVenta.CONFIRMADA,
                LocalDateTime.now(),
                List.of()
        );
    }

    private PromocionResponse crearPromocionResponse(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return new PromocionResponse(
                1L,
                "Descuento bebidas",
                "Promoción de prueba",
                1L,
                TipoPromocion.PORCENTAJE,
                new BigDecimal("10"),
                inicio,
                fin,
                true
        );
    }
}