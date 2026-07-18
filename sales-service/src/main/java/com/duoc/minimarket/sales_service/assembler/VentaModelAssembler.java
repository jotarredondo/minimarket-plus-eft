package com.duoc.minimarket.sales_service.assembler;

import com.duoc.minimarket.sales_service.controller.PedidoController;
import com.duoc.minimarket.sales_service.controller.VentaController;
import com.duoc.minimarket.sales_service.dto.VentaResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler {

    public EntityModel<VentaResponse> toModel(
            VentaResponse venta
    ) {
        EntityModel<VentaResponse> model =
                EntityModel.of(venta);

        model.add(
                linkTo(
                        methodOn(VentaController.class)
                                .obtenerPorId(
                                        venta.id()
                                )
                ).withSelfRel()
        );

        model.add(
                linkTo(
                        methodOn(PedidoController.class)
                                .obtenerParaGestion(
                                        venta.pedidoId()
                                )
                ).withRel("pedido")
        );

        model.add(
                linkTo(
                        methodOn(VentaController.class)
                                .listarTodas()
                ).withRel("todas-las-ventas")
        );

        return model;
    }

    public CollectionModel<EntityModel<VentaResponse>>
    toCollectionModel(
            List<VentaResponse> ventas
    ) {
        List<EntityModel<VentaResponse>> modelos =
                ventas.stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(modelos);
    }
}
