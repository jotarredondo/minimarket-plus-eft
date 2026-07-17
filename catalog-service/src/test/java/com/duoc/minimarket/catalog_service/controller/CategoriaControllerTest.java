package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.service.CategoriaService;
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
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    private CategoriaController categoriaController;

    private CategoriaRequest request;
    private CategoriaResponse response;

    @BeforeEach
    void configurarDatos() {
        categoriaController =
                new CategoriaController(categoriaService);

        request = new CategoriaRequest(
                "Bebidas",
                "Bebidas, refrescos y jugos"
        );

        response = new CategoriaResponse(
                1L,
                "Bebidas",
                "Bebidas, refrescos y jugos",
                true
        );
    }

    @Test
    void listar_debeRetornarCategoriasActivas() {
        when(categoriaService.listarActivas())
                .thenReturn(List.of(response));

        ResponseEntity<List<CategoriaResponse>> resultado =
                categoriaController.listar();

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
                        "Bebidas",
                        resultado.getBody().get(0).nombre()
                )
        );

        verify(categoriaService).listarActivas();
    }

    @Test
    void obtenerPorId_debeRetornarCategoria() {
        when(categoriaService.obtenerPorId(1L))
                .thenReturn(response);

        ResponseEntity<CategoriaResponse> resultado =
                categoriaController.obtenerPorId(1L);

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
                        "Bebidas",
                        resultado.getBody().nombre()
                )
        );

        verify(categoriaService).obtenerPorId(1L);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(categoriaService.crear(request))
                .thenReturn(response);

        ResponseEntity<CategoriaResponse> resultado =
                categoriaController.crear(request);

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

        verify(categoriaService).crear(request);
    }

    @Test
    void actualizar_debeRetornarCategoriaActualizada() {
        CategoriaResponse actualizada =
                new CategoriaResponse(
                        1L,
                        "Bebidas Actualizadas",
                        "Nueva descripción",
                        true
                );

        when(categoriaService.actualizar(1L, request))
                .thenReturn(actualizada);

        ResponseEntity<CategoriaResponse> resultado =
                categoriaController.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        "Bebidas Actualizadas",
                        resultado.getBody().nombre()
                )
        );

        verify(categoriaService).actualizar(1L, request);
    }

    @Test
    void cambiarEstado_debeRetornarCategoriaDesactivada() {
        CategoriaResponse desactivada =
                new CategoriaResponse(
                        1L,
                        "Bebidas",
                        "Bebidas, refrescos y jugos",
                        false
                );

        when(categoriaService.cambiarEstado(1L, false))
                .thenReturn(desactivada);

        ResponseEntity<CategoriaResponse> resultado =
                categoriaController.cambiarEstado(1L, false);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertFalse(
                        resultado.getBody().activo()
                )
        );

        verify(categoriaService).cambiarEstado(1L, false);
    }
}
