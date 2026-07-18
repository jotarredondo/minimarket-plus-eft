package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.CategoriaModelAssembler;
import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.service.CategoriaService;
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
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private CategoriaModelAssembler categoriaModelAssembler;

    private CategoriaController categoriaController;

    private CategoriaRequest request;
    private CategoriaResponse categoriaResponse;
    private EntityModel<CategoriaResponse> categoriaModel;

    @BeforeEach
    void configurarDatos() {
        categoriaController =
                new CategoriaController(
                        categoriaService,
                        categoriaModelAssembler
                );

        request = new CategoriaRequest(
                "Bebidas",
                "Bebidas, refrescos y jugos"
        );

        categoriaResponse = new CategoriaResponse(
                1L,
                "Bebidas",
                "Bebidas, refrescos y jugos",
                true
        );

        categoriaModel =
                EntityModel.of(categoriaResponse);
    }

    @Test
    void listar_debeRetornarCategoriasActivas() {
        List<CategoriaResponse> categorias =
                List.of(categoriaResponse);

        CollectionModel<EntityModel<CategoriaResponse>> modelo =
                CollectionModel.of(
                        List.of(categoriaModel)
                );

        when(categoriaService.listarActivas())
                .thenReturn(categorias);

        when(categoriaModelAssembler
                .toCollectionModel(categorias))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<CategoriaResponse>>
                > resultado =
                categoriaController.listar();

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

        verify(categoriaService).listarActivas();

        verify(categoriaModelAssembler)
                .toCollectionModel(categorias);
    }

    @Test
    void obtenerPorId_debeRetornarCategoriaConEnlaces() {
        when(categoriaService.obtenerPorId(1L))
                .thenReturn(categoriaResponse);

        when(categoriaModelAssembler.toModel(categoriaResponse))
                .thenReturn(categoriaModel);

        ResponseEntity<EntityModel<CategoriaResponse>> resultado =
                categoriaController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        categoriaModel,
                        resultado.getBody()
                ),
                () -> assertEquals(
                        "Bebidas",
                        resultado.getBody()
                                .getContent()
                                .nombre()
                )
        );

        verify(categoriaService).obtenerPorId(1L);
        verify(categoriaModelAssembler)
                .toModel(categoriaResponse);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(categoriaService.crear(request))
                .thenReturn(categoriaResponse);

        when(categoriaModelAssembler.toModel(categoriaResponse))
                .thenReturn(categoriaModel);

        ResponseEntity<EntityModel<CategoriaResponse>> resultado =
                categoriaController.crear(request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        categoriaModel,
                        resultado.getBody()
                )
        );

        verify(categoriaService).crear(request);
        verify(categoriaModelAssembler)
                .toModel(categoriaResponse);
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

        EntityModel<CategoriaResponse> modeloActualizado =
                EntityModel.of(actualizada);

        when(categoriaService.actualizar(1L, request))
                .thenReturn(actualizada);

        when(categoriaModelAssembler.toModel(actualizada))
                .thenReturn(modeloActualizado);

        ResponseEntity<EntityModel<CategoriaResponse>> resultado =
                categoriaController.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        "Bebidas Actualizadas",
                        resultado.getBody()
                                .getContent()
                                .nombre()
                )
        );

        verify(categoriaService).actualizar(1L, request);
        verify(categoriaModelAssembler).toModel(actualizada);
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

        EntityModel<CategoriaResponse> modeloDesactivado =
                EntityModel.of(desactivada);

        when(categoriaService.cambiarEstado(1L, false))
                .thenReturn(desactivada);

        when(categoriaModelAssembler.toModel(desactivada))
                .thenReturn(modeloDesactivado);

        ResponseEntity<EntityModel<CategoriaResponse>> resultado =
                categoriaController.cambiarEstado(1L, false);

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

        verify(categoriaService).cambiarEstado(1L, false);
        verify(categoriaModelAssembler).toModel(desactivada);
    }
}
