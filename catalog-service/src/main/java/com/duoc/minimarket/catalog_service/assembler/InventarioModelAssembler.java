package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.controller.InventarioController;
import com.duoc.minimarket.catalog_service.controller.OrdenReposicionController;
import com.duoc.minimarket.catalog_service.controller.ProductoController;
import com.duoc.minimarket.catalog_service.controller.SucursalController;
import com.duoc.minimarket.catalog_service.dto.InventarioResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventarioModelAssembler
        implements RepresentationModelAssembler<
        InventarioResponse,
        EntityModel<InventarioResponse>
        > {

    @Override
    public EntityModel<InventarioResponse> toModel(
            InventarioResponse inventario
    ) {
        EntityModel<InventarioResponse> modelo =
                EntityModel.of(inventario);

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .obtenerPorId(inventario.id())
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(
                                        inventario.productoId()
                                )
                ).withRel("producto")
        );

        modelo.add(
                linkTo(
                        methodOn(SucursalController.class)
                                .obtenerPorId(
                                        inventario.sucursalId()
                                )
                ).withRel("sucursal")
        );

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarMovimientos(
                                        inventario.id()
                                )
                ).withRel("movimientos")
        );

        modelo.add(
                linkTo(
                        methodOn(OrdenReposicionController.class)
                                .listarPorInventario(
                                        inventario.id()
                                )
                ).withRel("ordenes-reposicion")
        );

        return modelo;
    }

    public CollectionModel<EntityModel<InventarioResponse>>
    toCollectionModelPorProducto(
            Iterable<? extends InventarioResponse> inventarios,
            Long productoId
    ) {
        CollectionModel<EntityModel<InventarioResponse>> modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(inventarios);

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorProducto(productoId)
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(productoId)
                ).withRel("producto")
        );

        return modelo;
    }

    public CollectionModel<EntityModel<InventarioResponse>>
    toCollectionModelPorSucursal(
            Iterable<? extends InventarioResponse> inventarios,
            Long sucursalId
    ) {
        CollectionModel<EntityModel<InventarioResponse>> modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(inventarios);

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorSucursal(sucursalId)
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(SucursalController.class)
                                .obtenerPorId(sucursalId)
                ).withRel("sucursal")
        );

        return modelo;
    }
}
