package com.duoc.minimarket.sales_service.assembler;


import com.duoc.minimarket.sales_service.controller.CarritoController;
import com.duoc.minimarket.sales_service.controller.PedidoController;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoModelAssembler {

    public EntityModel<CarritoResponse> toModel(
            CarritoResponse carrito
    ) {
        EntityModel<CarritoResponse> model =
                EntityModel.of(carrito);

        model.add(
                linkTo(
                        methodOn(CarritoController.class)
                                .obtenerActivo(null)
                ).withSelfRel()
        );

        model.add(
                linkTo(
                        methodOn(CarritoController.class)
                                .listarHistorial(null)
                ).withRel("historial")
        );

        model.add(
                linkTo(
                        methodOn(PedidoController.class)
                                .crear(
                                        null,
                                        null,
                                        null
                                )
                ).withRel("crear-pedido")
        );

        return model;
    }

    public CollectionModel<EntityModel<CarritoResponse>>
    toCollectionModel(
            List<CarritoResponse> carritos
    ) {
        List<EntityModel<CarritoResponse>> modelos =
                carritos.stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(
                modelos,
                linkTo(
                        methodOn(CarritoController.class)
                                .listarHistorial(null)
                ).withSelfRel()
        );
    }
}
