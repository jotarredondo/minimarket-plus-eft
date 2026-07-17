package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private ProductoController productoController;

    private ProductoRequest productoRequest;
    private ProductoResponse productoResponse;

    @BeforeEach
    void configurarDatos() {
        productoController =
                new ProductoController(productoService);

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
    }

    @Test
    void listar_sinCategoriaDebeRetornarTodosLosProductos() {
        when(productoService.listarActivos())
                .thenReturn(List.of(productoResponse));

        ResponseEntity<List<ProductoResponse>> respuesta =
                productoController.listar(null);

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
                        "BEB-001",
                        respuesta.getBody().get(0).sku()
                )
        );

        verify(productoService).listarActivos();
    }

    @Test
    void listar_conCategoriaDebeFiltrarPorCategoria() {
        when(productoService.listarPorCategoria(1L))
                .thenReturn(List.of(productoResponse));

        ResponseEntity<List<ProductoResponse>> respuesta =
                productoController.listar(1L);

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
                                .categoriaId()
                )
        );

        verify(productoService).listarPorCategoria(1L);
    }

    @Test
    void obtenerPorId_debeRetornarProducto() {
        when(productoService.obtenerPorId(1L))
                .thenReturn(productoResponse);

        ResponseEntity<ProductoResponse> respuesta =
                productoController.obtenerPorId(1L);

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
                        respuesta.getBody().sku()
                )
        );

        verify(productoService).obtenerPorId(1L);
    }

    @Test
    void crear_debeRetornar201Created() {
        when(productoService.crear(productoRequest))
                .thenReturn(productoResponse);

        ResponseEntity<ProductoResponse> respuesta =
                productoController.crear(productoRequest);

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        productoResponse,
                        respuesta.getBody()
                )
        );

        verify(productoService).crear(productoRequest);
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

        when(productoService.actualizar(
                1L,
                productoRequest
        )).thenReturn(actualizado);

        ResponseEntity<ProductoResponse> respuesta =
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
                        respuesta.getBody().nombre()
                ),
                () -> assertEquals(
                        new BigDecimal("2190.00"),
                        respuesta.getBody().precio()
                )
        );

        verify(productoService).actualizar(
                1L,
                productoRequest
        );
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

        when(productoService.cambiarEstado(1L, false))
                .thenReturn(desactivado);

        ResponseEntity<ProductoResponse> respuesta =
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
                        respuesta.getBody().activo()
                )
        );

        verify(productoService).cambiarEstado(
                1L,
                false
        );
    }
}
