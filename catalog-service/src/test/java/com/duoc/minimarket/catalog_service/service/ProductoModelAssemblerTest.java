package com.duoc.minimarket.catalog_service.service;


import com.duoc.minimarket.catalog_service.assembler.ProductoModelAssembler;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoModelAssemblerTest {

    private ProductoModelAssembler productoModelAssembler;
    private ProductoResponse productoResponse;

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

        productoModelAssembler =
                new ProductoModelAssembler();

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

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModel_debeAgregarEnlacesDelProducto() {
        EntityModel<ProductoResponse> modelo =
                productoModelAssembler.toModel(
                        productoResponse
                );

        assertEquals(
                productoResponse,
                modelo.getContent()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("productos")
        );

        assertTrue(
                modelo.hasLink("categoria")
        );

        assertTrue(
                modelo.hasLink("inventarios")
        );
    }

    @Test
    void toModel_noDebeAgregarCategoriaCuandoEsNull() {
        ProductoResponse sinCategoria =
                new ProductoResponse(
                        2L,
                        "GEN-001",
                        "Producto general",
                        null,
                        new BigDecimal("1000.00"),
                        true,
                        null,
                        null
                );

        EntityModel<ProductoResponse> modelo =
                productoModelAssembler.toModel(
                        sinCategoria
                );

        assertFalse(
                modelo.hasLink("categoria")
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("inventarios")
        );
    }

    @Test
    void toCollectionModel_debeAgregarEnlaceDeColeccion() {
        CollectionModel<EntityModel<ProductoResponse>> modelo =
                productoModelAssembler.toCollectionModel(
                        List.of(productoResponse)
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
