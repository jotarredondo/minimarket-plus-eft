package com.duoc.minimarket.sales_service.assembler;

import com.duoc.minimarket.sales_service.controller.PedidoController;
import com.duoc.minimarket.sales_service.controller.VentaController;
import com.duoc.minimarket.sales_service.dto.PedidoResponse;
import com.duoc.minimarket.sales_service.entity.EstadoPedido;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PedidoModelAssembler {

    public EntityModel<PedidoResponse> toModel(
            PedidoResponse pedido
    ) {
        EntityModel<PedidoResponse> model =
                EntityModel.of(pedido);

        model.add(
                linkTo(
                        methodOn(PedidoController.class)
                                .obtenerPedidoCliente(
                                        pedido.id(),
                                        null
                                )
                ).withSelfRel()
        );

        model.add(
                linkTo(
                        methodOn(PedidoController.class)
                                .listarPedidosCliente(null)
                ).withRel("mis-pedidos")
        );

        model.add(
                linkTo(
                        methodOn(PedidoController.class)
                                .obtenerParaGestion(
                                        pedido.id()
                                )
                ).withRel("gestion")
        );

        if (pedido.estado() == EstadoPedido.PENDIENTE) {
            model.add(
                    linkTo(
                            methodOn(VentaController.class)
                                    .confirmarVenta(
                                            pedido.id(),
                                            null,
                                            null
                                    )
                    ).withRel("confirmar-venta")
            );
        }

        return model;
    }

    public CollectionModel<EntityModel<PedidoResponse>>
    toCollectionModel(
            List<PedidoResponse> pedidos
    ) {
        List<EntityModel<PedidoResponse>> modelos =
                pedidos.stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(modelos);
    }
}
