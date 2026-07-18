package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioResponse;
import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimientoInventarioModelAssemblerTest {

    private MovimientoInventarioModelAssembler
            movimientoInventarioModelAssembler;

    private MovimientoInventarioResponse movimientoResponse;

    @BeforeEach
    void configurarDatos() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8082);
        request.setContextPath("");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        movimientoInventarioModelAssembler =
                new MovimientoInventarioModelAssembler();

        movimientoResponse =
                new MovimientoInventarioResponse(
                        1L,
                        1L,
                        TipoMovimientoInventario.SALIDA,
                        12,
                        20,
                        8,
                        "Salida por venta",
                        "admin@minimarket.cl",
                        LocalDateTime.of(
                                2026,
                                7,
                                18,
                                10,
                                5
                        )
                );
    }

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModel_debeAgregarEnlacesDelMovimiento() {
        EntityModel<MovimientoInventarioResponse> modelo =
                movimientoInventarioModelAssembler.toModel(
                        movimientoResponse
                );

        assertEquals(
                movimientoResponse,
                modelo.getContent()
        );

        assertTrue(
                modelo.hasLink("inventario")
        );

        assertTrue(
                modelo.hasLink("movimientos")
        );
    }

    @Test
    void toCollectionModel_debeAgregarEnlacesDeColeccion() {
        CollectionModel<
                EntityModel<MovimientoInventarioResponse>
                > modelo =
                movimientoInventarioModelAssembler
                        .toCollectionModel(
                                List.of(movimientoResponse),
                                1L
                        );

        assertEquals(
                1,
                modelo.getContent().size()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("inventario")
        );
    }
}
