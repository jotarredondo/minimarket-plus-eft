package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.ProductoModelAssembler;
import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private ProductoModelAssembler productoModelAssembler;

    private ProductoController productoController;

    private ProductoRequest productoRequest;
    private ProductoResponse productoResponse;
    private EntityModel<ProductoResponse> productoModel;

    @BeforeEach
    void configurarDatos() {
        productoController =
                new ProductoController(
                        productoService,
                        productoModelAssembler
                );

        productoRequest = new ProductoRequest(
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida gaseosa sabor cola",
                new BigDecimal("1990.00"),
                1L
        );

        productoResponse = new ProductoResponse(
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida gaseosa sabor cola",
                new BigDecimal("1990.00"),
                true,
                1L,
                "Bebidas"
        );

        productoModel =
                EntityModel.of(productoResponse);
    }

    @Test
    void listar_sinCategoriaDebeRetornarTodosLosProductos() {
        List<ProductoResponse> productos =
                List.of(productoResponse);

        CollectionModel<EntityModel<ProductoResponse>> modelo =
                CollectionModel.of(
                        List.of(productoModel)
                );

        when(productoService.listarActivos())
                .thenReturn(productos);

        when(productoModelAssembler
                .toCollectionModel(productos))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<ProductoResponse>>
                > respuesta =
                productoController.listar(null);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        modelo,
                        respuesta.getBody()
                ),
                () -> assertEquals(
                        1,
                        respuesta.getBody()
                                .getContent()
                                .size()
                )
        );

        verify(productoService).listarActivos();

        verify(productoModelAssembler)
                .toCollectionModel(productos);
    }

    @Test
    void listar_conCategoriaDebeFiltrarPorCategoria() {
        List<ProductoResponse> productos =
                List.of(productoResponse);

        CollectionModel<EntityModel<ProductoResponse>> modelo =
                CollectionModel.of(
                        List.of(productoModel)
                );

        when(productoService.listarPorCategoria(1L))
                .thenReturn(productos);

        when(productoModelAssembler
                .toCollectionModel(productos))
                .thenReturn(modelo);

        ResponseEntity<
                CollectionModel<EntityModel<ProductoResponse>>
                > respuesta =
                productoController.listar(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        modelo,
                        respuesta.getBody()
                )
        );

        verify(productoService)
                .listarPorCategoria(1L);

        verify(productoModelAssembler)
                .toCollectionModel(productos);
    }

    @Test
    void obtenerPorId_debeRetornarProductoConEnlaces() {
        when(productoService.obtenerPorId(1L))
                .thenReturn(productoResponse);

        when(productoModelAssembler.toModel(productoResponse))
                .thenReturn(productoModel);

        ResponseEntity<EntityModel<ProductoResponse>> respuesta =
                productoController.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        productoModel,
                        respuesta.getBody()
                ),
                () -> assertEquals(
                        "BEB-001",
                        respuesta.getBody()
                                .getContent()
                                .sku()
                )
        );

        verify(productoService).obtenerPorId(1L);
        verify(productoModelAssembler)
                .toModel(productoResponse);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(productoService.crear(productoRequest))
                .thenReturn(productoResponse);

        when(productoModelAssembler.toModel(productoResponse))
                .thenReturn(productoModel);

        ResponseEntity<EntityModel<ProductoResponse>> respuesta =
                productoController.crear(productoRequest);

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        productoModel,
                        respuesta.getBody()
                )
        );

        verify(productoService).crear(productoRequest);
        verify(productoModelAssembler)
                .toModel(productoResponse);
    }

    @Test
    void actualizar_debeRetornarProductoActualizado() {
        ProductoResponse actualizado =
                new ProductoResponse(
                        1L,
                        "BEB-001",
                        "Bebida Cola Actualizada",
                        "Descripción actualizada",
                        new BigDecimal("2190.00"),
                        true,
                        1L,
                        "Bebidas"
                );

        EntityModel<ProductoResponse> modeloActualizado =
                EntityModel.of(actualizado);

        when(productoService.actualizar(
                1L,
                productoRequest
        )).thenReturn(actualizado);

        when(productoModelAssembler.toModel(actualizado))
                .thenReturn(modeloActualizado);

        ResponseEntity<EntityModel<ProductoResponse>> respuesta =
                productoController.actualizar(
                        1L,
                        productoRequest
                );

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        "Bebida Cola Actualizada",
                        respuesta.getBody()
                                .getContent()
                                .nombre()
                )
        );

        verify(productoService).actualizar(
                1L,
                productoRequest
        );

        verify(productoModelAssembler)
                .toModel(actualizado);
    }

    @Test
    void cambiarEstado_debeRetornarProductoDesactivado() {
        ProductoResponse desactivado =
                new ProductoResponse(
                        1L,
                        "BEB-001",
                        "Bebida Cola 1.5 L",
                        "Bebida gaseosa sabor cola",
                        new BigDecimal("1990.00"),
                        false,
                        1L,
                        "Bebidas"
                );

        EntityModel<ProductoResponse> modeloDesactivado =
                EntityModel.of(desactivado);

        when(productoService.cambiarEstado(1L, false))
                .thenReturn(desactivado);

        when(productoModelAssembler.toModel(desactivado))
                .thenReturn(modeloDesactivado);

        ResponseEntity<EntityModel<ProductoResponse>> respuesta =
                productoController.cambiarEstado(
                        1L,
                        false
                );

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        respuesta.getStatusCode()
                ),
                () -> assertFalse(
                        respuesta.getBody()
                                .getContent()
                                .activo()
                )
        );

        verify(productoService)
                .cambiarEstado(1L, false);

        verify(productoModelAssembler)
                .toModel(desactivado);
    }
}
