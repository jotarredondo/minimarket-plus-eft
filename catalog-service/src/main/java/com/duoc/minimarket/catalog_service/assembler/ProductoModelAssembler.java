package com.duoc.minimarket.catalog_service.assembler;

import com.duoc.minimarket.catalog_service.controller.CategoriaController;
import com.duoc.minimarket.catalog_service.controller.InventarioController;
import com.duoc.minimarket.catalog_service.controller.ProductoController;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<
        ProductoResponse,
        EntityModel<ProductoResponse>
        > {

    @Override
    public EntityModel<ProductoResponse> toModel(
            ProductoResponse producto
    ) {
        EntityModel<ProductoResponse> modelo =
                EntityModel.of(producto);

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(producto.id())
                ).withSelfRel()
        );

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listar(null)
                ).withRel("productos")
        );

        if (producto.categoriaId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(CategoriaController.class)
                                    .obtenerPorId(
                                            producto.categoriaId()
                                    )
                    ).withRel("categoria")
            );
        }

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorProducto(producto.id())
                ).withRel("inventarios")
        );

        return modelo;
    }

    @Override
    public CollectionModel<EntityModel<ProductoResponse>>
    toCollectionModel(
            Iterable<? extends ProductoResponse> productos
    ) {
        CollectionModel<EntityModel<ProductoResponse>> modelo =
                RepresentationModelAssembler.super
                        .toCollectionModel(productos);

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listar(null)
                ).withSelfRel()
        );

        return modelo;
    }
}
