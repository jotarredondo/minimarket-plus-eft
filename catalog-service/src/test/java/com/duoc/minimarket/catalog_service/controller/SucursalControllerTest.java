package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.SucursalModelAssembler;
import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.service.SucursalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalControllerTest {

    @Mock
    private SucursalService sucursalService;

    @Mock
    private SucursalModelAssembler sucursalModelAssembler;

    private SucursalController sucursalController;

    private SucursalRequest request;
    private SucursalResponse sucursalResponse;
    private EntityModel<SucursalResponse> sucursalModel;

    @BeforeEach
    void configurarDatos() {
        sucursalController =
                new SucursalController(
                        sucursalService,
                        sucursalModelAssembler
                );

        request = new SucursalRequest(
                "SUC-001",
                "Sucursal Centro",
                "Avenida Principal 100"
        );

        sucursalResponse = new SucursalResponse(
                1L,
                "SUC-001",
                "Sucursal Centro",
                "Avenida Principal 100",
                true
        );

        sucursalModel =
                EntityModel.of(sucursalResponse);
    }

    @Test
    void listar_debeRetornarSucursalesActivas() {
        List<SucursalResponse> sucursales =
                List.of(sucursalResponse);

        CollectionModel<EntityModel<SucursalResponse>> modelo =
                CollectionModel.of(
                        List.of(sucursalModel)
                );

        when(sucursalService.listarActivas())
                .thenReturn(sucursales);

        when(sucursalModelAssembler
                .toCollectionModel(sucursales))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<SucursalResponse>>
                > resultado =
                sucursalController.listar();

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        modelo,
                        resultado.getBody()
                ),
                () -> assertEquals(
                        1,
                        resultado.getBody()
                                .getContent()
                                .size()
                )
        );

        verify(sucursalService).listarActivas();

        verify(sucursalModelAssembler)
                .toCollectionModel(sucursales);
    }

    @Test
    void obtenerPorId_debeRetornarSucursalConEnlaces() {
        when(sucursalService.obtenerPorId(1L))
                .thenReturn(sucursalResponse);

        when(sucursalModelAssembler.toModel(sucursalResponse))
                .thenReturn(sucursalModel);

        ResponseEntity<EntityModel<SucursalResponse>> resultado =
                sucursalController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        sucursalModel,
                        resultado.getBody()
                ),
                () -> assertEquals(
                        "SUC-001",
                        resultado.getBody()
                                .getContent()
                                .codigo()
                )
        );

        verify(sucursalService).obtenerPorId(1L);
        verify(sucursalModelAssembler)
                .toModel(sucursalResponse);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(sucursalService.crear(request))
                .thenReturn(sucursalResponse);

        when(sucursalModelAssembler.toModel(sucursalResponse))
                .thenReturn(sucursalModel);

        ResponseEntity<EntityModel<SucursalResponse>> resultado =
                sucursalController.crear(request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        sucursalModel,
                        resultado.getBody()
                )
        );

        verify(sucursalService).crear(request);
        verify(sucursalModelAssembler)
                .toModel(sucursalResponse);
    }

    @Test
    void actualizar_debeRetornarSucursalActualizada() {
        SucursalResponse actualizada =
                new SucursalResponse(
                        1L,
                        "SUC-001",
                        "Sucursal Centro Actualizada",
                        "Nueva Avenida 200",
                        true
                );

        EntityModel<SucursalResponse> modeloActualizado =
                EntityModel.of(actualizada);

        when(sucursalService.actualizar(1L, request))
                .thenReturn(actualizada);

        when(sucursalModelAssembler.toModel(actualizada))
                .thenReturn(modeloActualizado);

        ResponseEntity<EntityModel<SucursalResponse>> resultado =
                sucursalController.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        "Sucursal Centro Actualizada",
                        resultado.getBody()
                                .getContent()
                                .nombre()
                ),
                () -> assertEquals(
                        "Nueva Avenida 200",
                        resultado.getBody()
                                .getContent()
                                .direccion()
                )
        );

        verify(sucursalService).actualizar(1L, request);
        verify(sucursalModelAssembler).toModel(actualizada);
    }

    @Test
    void cambiarEstado_debeRetornarSucursalDesactivada() {
        SucursalResponse desactivada =
                new SucursalResponse(
                        1L,
                        "SUC-001",
                        "Sucursal Centro",
                        "Avenida Principal 100",
                        false
                );

        EntityModel<SucursalResponse> modeloDesactivado =
                EntityModel.of(desactivada);

        when(sucursalService.cambiarEstado(1L, false))
                .thenReturn(desactivada);

        when(sucursalModelAssembler.toModel(desactivada))
                .thenReturn(modeloDesactivado);

        ResponseEntity<EntityModel<SucursalResponse>> resultado =
                sucursalController.cambiarEstado(1L, false);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertFalse(
                        resultado.getBody()
                                .getContent()
                                .activo()
                )
        );

        verify(sucursalService).cambiarEstado(1L, false);
        verify(sucursalModelAssembler).toModel(desactivada);
    }
}