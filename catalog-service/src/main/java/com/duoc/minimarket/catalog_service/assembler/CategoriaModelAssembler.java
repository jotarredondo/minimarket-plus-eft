package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.controller.CategoriaController;
import com.duoc.minimarket.catalog_service.controller.ProductoController;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<
        CategoriaResponse,
        EntityModel<CategoriaResponse>
        > {

    @Override
    public EntityModel<CategoriaResponse> toModel(
            CategoriaResponse categoria
    ) {
        EntityModel<CategoriaResponse> modelo =
                EntityModel.of(categoria);

        modelo.add(
                linkTo(
                        methodOn(CategoriaController.class)
                                .obtenerPorId(categoria.id())
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(CategoriaController.class)
                                .listar()
                ).withRel("categorias")
        );

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listar(categoria.id())
                ).withRel("productos")
        );

        return modelo;
    }

    @Override
    public CollectionModel<EntityModel<CategoriaResponse>>
    toCollectionModel(
            Iterable<? extends CategoriaResponse> categorias
    ) {
        CollectionModel<EntityModel<CategoriaResponse>> modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(categorias);

        modelo.add(
                linkTo(
                        methodOn(CategoriaController.class)
                                .listar()
                ).withSelfRel()
        );

        return modelo;
    }
}