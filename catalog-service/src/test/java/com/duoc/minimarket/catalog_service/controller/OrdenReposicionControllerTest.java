package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.ActualizarOrdenReposicionRequest;
import com.duoc.minimarket.catalog_service.dto.OrdenReposicionResponse;
import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenReposicionControllerTest {

    @Mock
    private InventarioService inventarioService;

    private OrdenReposicionController controller;
    private OrdenReposicionResponse ordenGenerada;

    @BeforeEach
    void configurarDatos() {
        controller =
                new OrdenReposicionController(inventarioService);

        ordenGenerada = new OrdenReposicionResponse(
                1L,
                1L,
                1L,
                "Bebida Cola 1.5 L",
                1L,
                "Sucursal Centro",
                12,
                EstadoOrdenReposicion.GENERADA,
                "Stock actual igual o inferior al stock mínimo",
                LocalDateTime.of(2026, 7, 17, 12, 0)
        );
    }

    @Test
    void listarPorEstado_debeRetornarOrdenes() {
        when(inventarioService.listarOrdenesPorEstado(
                EstadoOrdenReposicion.GENERADA
        )).thenReturn(List.of(ordenGenerada));

        ResponseEntity<List<OrdenReposicionResponse>> resultado =
                controller.listarPorEstado(
                        EstadoOrdenReposicion.GENERADA
                );

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
                        EstadoOrdenReposicion.GENERADA,
                        resultado.getBody().get(0).estado()
                ),
                () -> assertEquals(
                        12,
                        resultado.getBody().get(0).cantidadSugerida()
                )
        );

        verify(inventarioService).listarOrdenesPorEstado(
                EstadoOrdenReposicion.GENERADA
        );
    }

    @Test
    void listarPorInventario_debeRetornarHistorialDeOrdenes() {
        when(inventarioService.listarOrdenesPorInventario(1L))
                .thenReturn(List.of(ordenGenerada));

        ResponseEntity<List<OrdenReposicionResponse>> resultado =
                controller.listarPorInventario(1L);

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
                        1L,
                        resultado.getBody().get(0).inventarioId()
                )
        );

        verify(inventarioService)
                .listarOrdenesPorInventario(1L);
    }

    @Test
    void actualizarEstado_debeRetornarOrdenProcesada() {
        ActualizarOrdenReposicionRequest request =
                new ActualizarOrdenReposicionRequest(
                        EstadoOrdenReposicion.PROCESADA
                );

        OrdenReposicionResponse procesada =
                new OrdenReposicionResponse(
                        1L,
                        1L,
                        1L,
                        "Bebida Cola 1.5 L",
                        1L,
                        "Sucursal Centro",
                        12,
                        EstadoOrdenReposicion.PROCESADA,
                        "Stock actual igual o inferior al stock mínimo",
                        LocalDateTime.of(2026, 7, 17, 12, 0)
                );

        when(inventarioService.actualizarEstadoOrden(
                1L,
                request
        )).thenReturn(procesada);

        ResponseEntity<OrdenReposicionResponse> resultado =
                controller.actualizarEstado(1L, request);

        assertAll(
                () -> assertEquals(
                        HttpStatus.OK,
                        resultado.getStatusCode()
                ),
                () -> assertEquals(
                        EstadoOrdenReposicion.PROCESADA,
                        resultado.getBody().estado()
                )
        );

        verify(inventarioService)
                .actualizarEstadoOrden(1L, request);
    }
}
