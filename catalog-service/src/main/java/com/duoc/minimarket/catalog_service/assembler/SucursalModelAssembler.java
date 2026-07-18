package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.controller.InventarioController;
import com.duoc.minimarket.catalog_service.controller.SucursalController;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SucursalModelAssembler
        implements RepresentationModelAssembler<
        SucursalResponse,
        EntityModel<SucursalResponse>
        > {

    @Override
    public EntityModel<SucursalResponse> toModel(
            SucursalResponse sucursal
    ) {
        EntityModel<SucursalResponse> modelo =
                EntityModel.of(sucursal);

        modelo.add(
                linkTo(
                        methodOn(SucursalController.class)
                                .obtenerPorId(sucursal.id())
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(SucursalController.class)
                                .listar()
                ).withRel("sucursales")
        );

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorSucursal(sucursal.id())
                ).withRel("inventarios")
        );

        return modelo;
    }

    @Override
    public CollectionModel<EntityModel<SucursalResponse>>
    toCollectionModel(
            Iterable<? extends SucursalResponse> sucursales
    ) {
        CollectionModel<EntityModel<SucursalResponse>> modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(sucursales);

        modelo.add(
                linkTo(
                        methodOn(SucursalController.class)
                                .listar()
                ).withSelfRel()
        );

        return modelo;
    }
}
