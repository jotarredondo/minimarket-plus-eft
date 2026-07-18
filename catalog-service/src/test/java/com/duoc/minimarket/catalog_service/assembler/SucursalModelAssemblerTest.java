package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SucursalModelAssemblerTest {

    private SucursalModelAssembler sucursalModelAssembler;
    private SucursalResponse sucursalResponse;

    @BeforeEach
    void configurarDatos() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8082);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        sucursalModelAssembler =
                new SucursalModelAssembler();

        sucursalResponse = new SucursalResponse(
                1L,
                "SUC-001",
                "Sucursal Centro",
                "Avenida Principal 100",
                true
        );
    }

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModel_debeAgregarEnlacesDeSucursal() {
        EntityModel<SucursalResponse> modelo =
                sucursalModelAssembler.toModel(
                        sucursalResponse
                );

        assertEquals(
                sucursalResponse,
                modelo.getContent()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("sucursales")
        );

        assertTrue(
                modelo.hasLink("inventarios")
        );
    }

    @Test
    void toCollectionModel_debeAgregarEnlaceDeColeccion() {
        CollectionModel<EntityModel<SucursalResponse>> modelo =
                sucursalModelAssembler.toCollectionModel(
                        List.of(sucursalResponse)
                );

        assertEquals(
                1,
                modelo.getContent().size()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );
    }
}
