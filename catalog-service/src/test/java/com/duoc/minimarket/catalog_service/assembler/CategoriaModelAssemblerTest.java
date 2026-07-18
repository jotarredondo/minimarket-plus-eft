package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
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

class CategoriaModelAssemblerTest {

    private CategoriaModelAssembler categoriaModelAssembler;
    private CategoriaResponse categoriaResponse;

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

        categoriaModelAssembler =
                new CategoriaModelAssembler();

        categoriaResponse = new CategoriaResponse(
                1L,
                "Bebidas",
                "Bebidas, refrescos y jugos",
                true
        );
    }

    @AfterEach
    void limpiarContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModel_debeAgregarEnlacesDeCategoria() {
        EntityModel<CategoriaResponse> modelo =
                categoriaModelAssembler.toModel(
                        categoriaResponse
                );

        assertEquals(
                categoriaResponse,
                modelo.getContent()
        );

        assertTrue(
                modelo.hasLink(IanaLinkRelations.SELF)
        );

        assertTrue(
                modelo.hasLink("categorias")
        );

        assertTrue(
                modelo.hasLink("productos")
        );
    }

    @Test
    void toCollectionModel_debeAgregarEnlaceDeColeccion() {
        CollectionModel<EntityModel<CategoriaResponse>> modelo =
                categoriaModelAssembler.toCollectionModel(
                        List.of(categoriaResponse)
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
