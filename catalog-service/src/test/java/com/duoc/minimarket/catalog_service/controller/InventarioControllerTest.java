package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.CrearInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.InventarioResponse;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioResponse;
import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;
import com.duoc.minimarket.catalog_service.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    private static final String ADMIN_EMAIL =
            "admin@minimarket.cl";

    @Mock
    private InventarioService inventarioService;

    @Mock
    private Authentication authentication;

    private InventarioController inventarioController;

    private CrearInventarioRequest crearInventarioRequest;
    private InventarioResponse inventarioResponse;

    @BeforeEach
    void configurarDatos() {
        inventarioController =
                new InventarioController(inventarioService);

        crearInventarioRequest =
                new CrearInventarioRequest(
                        1L,
                        1L,
                        20,
                        10
                );

        inventarioResponse =
                new InventarioResponse(
                        1L,
                        1L,
                        "BEB-001",
                        "Bebida Cola 1.5 L",
                        1L,
                        "SUC-001",
                        "Sucursal Centro",
                        20,
                        10,
                        false,
                        LocalDateTime.of(
                                2026,
                                7,
                                17,
                                10,
                                0
                        )
                );
    }

    @Test
    void crear_debeRetornar201Created() {
        when(authentication.getName())
                .thenReturn(ADMIN_EMAIL);

        when(inventarioService.crear(
                crearInventarioRequest,
                ADMIN_EMAIL
        )).thenReturn(inventarioResponse);

        ResponseEntity<InventarioResponse> respuesta =
                inventarioController.crear(
                        crearInventarioRequest,
                        authentication
                );

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        inventarioResponse,
                        respuesta.getBody()
                ),
                () -> assertEquals(
                        20,
                        respuesta.getBody().stockActual()
                ),
                () -> assertEquals(
                        false,
                        respuesta.getBody().requiereReposicion()
                )
        );

        verify(authentication).getName();

        verify(inventarioService).crear(
                crearInventarioRequest,
                ADMIN_EMAIL
        );
    }

    @Test
    void obtenerPorId_debeRetornarInventario() {
        when(inventarioService.obtenerPorId(1L))
                .thenReturn(inventarioResponse);

        ResponseEntity<InventarioResponse> respuesta =
                inventarioController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1L,
                        respuesta.getBody().id()
                ),
                () -> assertEquals(
                        "BEB-001",
                        respuesta.getBody().productoSku()
                ),
                () -> assertEquals(
                        "SUC-001",
                        respuesta.getBody().sucursalCodigo()
                )
        );

        verify(inventarioService).obtenerPorId(1L);
    }

    @Test
    void listarPorProducto_debeRetornarInventariosDelProducto() {
        when(inventarioService.listarPorProducto(1L))
                .thenReturn(List.of(inventarioResponse));

        ResponseEntity<List<InventarioResponse>> respuesta =
                inventarioController.listarPorProducto(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody().size()
                ),
                () -> assertEquals(
                        1L,
                        respuesta.getBody()
                                .get(0)
                                .productoId()
                )
        );

        verify(inventarioService).listarPorProducto(1L);
    }

    @Test
    void listarPorSucursal_debeRetornarInventariosDeSucursal() {
        when(inventarioService.listarPorSucursal(1L))
                .thenReturn(List.of(inventarioResponse));

        ResponseEntity<List<InventarioResponse>> respuesta =
                inventarioController.listarPorSucursal(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody().size()
                ),
                () -> assertEquals(
                        1L,
                        respuesta.getBody()
                                .get(0)
                                .sucursalId()
                ),
                () -> assertEquals(
                        "Sucursal Centro",
                        respuesta.getBody()
                                .get(0)
                                .sucursalNombre()
                )
        );

        verify(inventarioService).listarPorSucursal(1L);
    }

    @Test
    void registrarMovimiento_debeRetornar201Created() {
        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        12,
                        "Salida por venta"
                );

        MovimientoInventarioResponse movimientoResponse =
                new MovimientoInventarioResponse(
                        1L,
                        1L,
                        TipoMovimientoInventario.SALIDA,
                        12,
                        20,
                        8,
                        "Salida por venta",
                        ADMIN_EMAIL,
                        LocalDateTime.of(
                                2026,
                                7,
                                17,
                                10,
                                5
                        )
                );

        when(authentication.getName())
                .thenReturn(ADMIN_EMAIL);

        when(inventarioService.registrarMovimiento(
                1L,
                request,
                ADMIN_EMAIL
        )).thenReturn(movimientoResponse);

        ResponseEntity<MovimientoInventarioResponse> respuesta =
                inventarioController.registrarMovimiento(
                        1L,
                        request,
                        authentication
                );

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        TipoMovimientoInventario.SALIDA,
                        respuesta.getBody().tipo()
                ),
                () -> assertEquals(
                        20,
                        respuesta.getBody().stockAnterior()
                ),
                () -> assertEquals(
                        8,
                        respuesta.getBody().stockPosterior()
                ),
                () -> assertEquals(
                        ADMIN_EMAIL,
                        respuesta.getBody().usuarioEmail()
                )
        );

        verify(authentication).getName();

        verify(inventarioService).registrarMovimiento(
                1L,
                request,
                ADMIN_EMAIL
        );
    }

    @Test
    void listarMovimientos_debeRetornarHistorial() {
        MovimientoInventarioResponse movimiento =
                new MovimientoInventarioResponse(
                        1L,
                        1L,
                        TipoMovimientoInventario.ENTRADA,
                        10,
                        8,
                        18,
                        "Recepción de mercadería",
                        ADMIN_EMAIL,
                        LocalDateTime.of(
                                2026,
                                7,
                                17,
                                11,
                                0
                        )
                );

        when(inventarioService.listarMovimientos(1L))
                .thenReturn(List.of(movimiento));

        ResponseEntity<List<MovimientoInventarioResponse>> respuesta =
                inventarioController.listarMovimientos(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody().size()
                ),
                () -> assertEquals(
                        TipoMovimientoInventario.ENTRADA,
                        respuesta.getBody()
                                .get(0)
                                .tipo()
                ),
                () -> assertEquals(
                        18,
                        respuesta.getBody()
                                .get(0)
                                .stockPosterior()
                )
        );

        verify(inventarioService).listarMovimientos(1L);
    }
}
