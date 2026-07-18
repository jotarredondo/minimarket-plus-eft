package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.controller.InventarioController;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MovimientoInventarioModelAssembler
        implements RepresentationModelAssembler<
        MovimientoInventarioResponse,
        EntityModel<MovimientoInventarioResponse>
        > {

    @Override
    public EntityModel<MovimientoInventarioResponse> toModel(
            MovimientoInventarioResponse movimiento
    ) {
        EntityModel<MovimientoInventarioResponse> modelo =
                EntityModel.of(movimiento);

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .obtenerPorId(
                                        movimiento.inventarioId()
                                )
                ).withRel("inventario")
        );

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarMovimientos(
                                        movimiento.inventarioId()
                                )
                ).withRel("movimientos")
        );

        return modelo;
    }

    public CollectionModel<
            EntityModel<MovimientoInventarioResponse>
            > toCollectionModel(
            Iterable<? extends MovimientoInventarioResponse> movimientos,
            Long inventarioId
    ) {
        CollectionModel<
                EntityModel<MovimientoInventarioResponse>
                > modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(movimientos);

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarMovimientos(inventarioId)
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .obtenerPorId(inventarioId)
                ).withRel("inventario")
        );

        return modelo;
    }
}
