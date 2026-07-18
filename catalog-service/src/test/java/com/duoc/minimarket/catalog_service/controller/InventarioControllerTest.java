package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.InventarioModelAssembler;
import com.duoc.minimarket.catalog_service.assembler.MovimientoInventarioModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
    private InventarioModelAssembler inventarioModelAssembler;

    @Mock
    private MovimientoInventarioModelAssembler
            movimientoModelAssembler;

    @Mock
    private Authentication authentication;

    private InventarioController inventarioController;

    private CrearInventarioRequest crearInventarioRequest;
    private InventarioResponse inventarioResponse;
    private EntityModel<InventarioResponse> inventarioModel;

    @BeforeEach
    void configurarDatos() {
        inventarioController =
                new InventarioController(
                        inventarioService,
                        inventarioModelAssembler,
                        movimientoModelAssembler
                );

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

        inventarioModel =
                EntityModel.of(inventarioResponse);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(authentication.getName())
                .thenReturn(ADMIN_EMAIL);

        when(inventarioService.crear(
                crearInventarioRequest,
                ADMIN_EMAIL
        )).thenReturn(inventarioResponse);

        when(inventarioModelAssembler
                .toModel(inventarioResponse))
                .thenReturn(inventarioModel);

        ResponseEntity<EntityModel<InventarioResponse>> respuesta =
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
                        inventarioModel,
                        respuesta.getBody()
                ),
                () -> assertEquals(
                        20,
                        respuesta.getBody()
                                .getContent()
                                .stockActual()
                )
        );

        verify(inventarioService).crear(
                crearInventarioRequest,
                ADMIN_EMAIL
        );

        verify(inventarioModelAssembler)
                .toModel(inventarioResponse);
    }

    @Test
    void obtenerPorId_debeRetornarInventarioConEnlaces() {
        when(inventarioService.obtenerPorId(1L))
                .thenReturn(inventarioResponse);

        when(inventarioModelAssembler
                .toModel(inventarioResponse))
                .thenReturn(inventarioModel);

        ResponseEntity<EntityModel<InventarioResponse>> respuesta =
                inventarioController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        inventarioModel,
                        respuesta.getBody()
                ),
                () -> assertEquals(
                        "BEB-001",
                        respuesta.getBody()
                                .getContent()
                                .productoSku()
                )
        );

        verify(inventarioService).obtenerPorId(1L);
        verify(inventarioModelAssembler)
                .toModel(inventarioResponse);
    }

    @Test
    void listarPorProducto_debeRetornarColeccion() {
        List<InventarioResponse> inventarios =
                List.of(inventarioResponse);

        CollectionModel<EntityModel<InventarioResponse>> modelo =
                CollectionModel.of(
                        List.of(inventarioModel)
                );

        when(inventarioService.listarPorProducto(1L))
                .thenReturn(inventarios);

        when(inventarioModelAssembler
                .toCollectionModelPorProducto(
                        inventarios,
                        1L
                ))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<InventarioResponse>>
                > respuesta =
                inventarioController.listarPorProducto(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody()
                                .getContent()
                                .size()
                )
        );

        verify(inventarioService)
                .listarPorProducto(1L);

        verify(inventarioModelAssembler)
                .toCollectionModelPorProducto(
                        inventarios,
                        1L
                );
    }

    @Test
    void listarPorSucursal_debeRetornarColeccion() {
        List<InventarioResponse> inventarios =
                List.of(inventarioResponse);

        CollectionModel<EntityModel<InventarioResponse>> modelo =
                CollectionModel.of(
                        List.of(inventarioModel)
                );

        when(inventarioService.listarPorSucursal(1L))
                .thenReturn(inventarios);

        when(inventarioModelAssembler
                .toCollectionModelPorSucursal(
                        inventarios,
                        1L
                ))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<InventarioResponse>>
                > respuesta =
                inventarioController.listarPorSucursal(1L);

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertEquals(
                1,
                respuesta.getBody()
                        .getContent()
                        .size()
        );

        verify(inventarioService)
                .listarPorSucursal(1L);

        verify(inventarioModelAssembler)
                .toCollectionModelPorSucursal(
                        inventarios,
                        1L
                );
    }

    @Test
    void registrarMovimiento_debeRetornar201Created() {
        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        12,
                        "Salida por venta"
                );

        MovimientoInventarioResponse movimiento =
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

        EntityModel<MovimientoInventarioResponse>
                movimientoModel =
                EntityModel.of(movimiento);

        when(authentication.getName())
                .thenReturn(ADMIN_EMAIL);

        when(inventarioService.registrarMovimiento(
                1L,
                request,
                ADMIN_EMAIL
        )).thenReturn(movimiento);

        when(movimientoModelAssembler.toModel(movimiento))
                .thenReturn(movimientoModel);

        ResponseEntity<
                EntityModel<MovimientoInventarioResponse>
                > respuesta =
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
                        8,
                        respuesta.getBody()
                                .getContent()
                                .stockPosterior()
                )
        );

        verify(inventarioService).registrarMovimiento(
                1L,
                request,
                ADMIN_EMAIL
        );

        verify(movimientoModelAssembler)
                .toModel(movimiento);
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

        List<MovimientoInventarioResponse> movimientos =
                List.of(movimiento);

        CollectionModel<
                EntityModel<MovimientoInventarioResponse>
                > modelo =
                CollectionModel.of(
                        List.of(EntityModel.of(movimiento))
                );

        when(inventarioService.listarMovimientos(1L))
                .thenReturn(movimientos);

        when(movimientoModelAssembler.toCollectionModel(
                movimientos,
                1L
        )).thenReturn(modelo);

        ResponseEntity<
                CollectionModel<
                        EntityModel<MovimientoInventarioResponse>
                        >
                > respuesta =
                inventarioController.listarMovimientos(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody()
                                .getContent()
                                .size()
                )
        );

        verify(inventarioService)
                .listarMovimientos(1L);

        verify(movimientoModelAssembler)
                .toCollectionModel(movimientos, 1L);
    }
}