package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.service.SucursalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private SucursalController sucursalController;

    private SucursalRequest request;
    private SucursalResponse response;

    @BeforeEach
    void configurarDatos() {
        sucursalController =
                new SucursalController(sucursalService);

        request = new SucursalRequest(
                "SUC-001",
                "Sucursal Centro",
                "Avenida Principal 100"
        );

        response = new SucursalResponse(
                1L,
                "SUC-001",
                "Sucursal Centro",
                "Avenida Principal 100",
                true
        );
    }

    @Test
    void listar_debeRetornarSucursalesActivas() {
        when(sucursalService.listarActivas())
                .thenReturn(List.of(response));

        ResponseEntity<List<SucursalResponse>> resultado =
                sucursalController.listar();

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        1,
                        resultado.getBody().size()
                ),
                () -> assertEquals(
                        "SUC-001",
                        resultado.getBody().get(0).codigo()
                )
        );

        verify(sucursalService).listarActivas();
    }

    @Test
    void obtenerPorId_debeRetornarSucursal() {
        when(sucursalService.obtenerPorId(1L))
                .thenReturn(response);

        ResponseEntity<SucursalResponse> resultado =
                sucursalController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        1L,
                        resultado.getBody().id()
                ),
                () -> assertEquals(
                        "Sucursal Centro",
                        resultado.getBody().nombre()
                )
        );

        verify(sucursalService).obtenerPorId(1L);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(sucursalService.crear(request))
                .thenReturn(response);

        ResponseEntity<SucursalResponse> resultado =
                sucursalController.crear(request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        response,
                        resultado.getBody()
                )
        );

        verify(sucursalService).crear(request);
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

        when(sucursalService.actualizar(1L, request))
                .thenReturn(actualizada);

        ResponseEntity<SucursalResponse> resultado =
                sucursalController.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        "Sucursal Centro Actualizada",
                        resultado.getBody().nombre()
                ),
                () -> assertEquals(
                        "Nueva Avenida 200",
                        resultado.getBody().direccion()
                )
        );

        verify(sucursalService).actualizar(1L, request);
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

        when(sucursalService.cambiarEstado(1L, false))
                .thenReturn(desactivada);

        ResponseEntity<SucursalResponse> resultado =
                sucursalController.cambiarEstado(1L, false);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertFalse(
                        resultado.getBody().activo()
                )
        );

        verify(sucursalService).cambiarEstado(1L, false);
    }
}