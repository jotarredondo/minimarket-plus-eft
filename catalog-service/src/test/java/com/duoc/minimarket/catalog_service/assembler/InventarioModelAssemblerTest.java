package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.dto.InventarioResponse;
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

class InventarioModelAssemblerTest {

    private InventarioModelAssembler inventarioModelAssembler;
    private InventarioResponse inventarioResponse;

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

        inventarioModelAssembler =
                new InventarioModelAssembler();

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
                                18,
                                10,
                                0
                        )
                );
    }

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModel_debeAgregarEnlacesDelInventario() {
        EntityModel<InventarioResponse> modelo =
                inventarioModelAssembler.toModel(
                        inventarioResponse
                );

        assertEquals(
                inventarioResponse,
                modelo.getContent()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("producto")
        );

        assertTrue(
                modelo.hasLink("sucursal")
        );

        assertTrue(
                modelo.hasLink("movimientos")
        );

        assertTrue(
                modelo.hasLink("ordenes-reposicion")
        );
    }

    @Test
    void toCollectionModelPorProducto_debeAgregarEnlaces() {
        CollectionModel<EntityModel<InventarioResponse>> modelo =
                inventarioModelAssembler
                        .toCollectionModelPorProducto(
                                List.of(inventarioResponse),
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
                modelo.hasLink("producto")
        );
    }

    @Test
    void toCollectionModelPorSucursal_debeAgregarEnlaces() {
        CollectionModel<EntityModel<InventarioResponse>> modelo =
                inventarioModelAssembler
                        .toCollectionModelPorSucursal(
                                List.of(inventarioResponse),
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
                modelo.hasLink("sucursal")
        );
    }
}
