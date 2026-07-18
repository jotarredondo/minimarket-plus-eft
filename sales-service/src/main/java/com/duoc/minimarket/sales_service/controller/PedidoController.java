package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.CrearPedidoRequest;
import com.duoc.minimarket.sales_service.dto.PedidoResponse;
import com.duoc.minimarket.sales_service.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(
            PedidoService pedidoService
    ) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody CrearPedidoRequest request,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        pedidoService.crearDesdeCarrito(
                                authentication.getName(),
                                request,
                                authorizationHeader
                        )
                );
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<PedidoResponse>>
    listarPedidosCliente(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoService.listarPedidosCliente(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/mis-pedidos/{pedidoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponse>
    obtenerPedidoCliente(
            @PathVariable Long pedidoId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoService.obtenerPedidoCliente(
                        pedidoId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    public ResponseEntity<List<PedidoResponse>>
    listarPendientes() {
        return ResponseEntity.ok(
                pedidoService.listarPendientes()
        );
    }

    @GetMapping("/{pedidoId}/gestion")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    public ResponseEntity<PedidoResponse>
    obtenerParaGestion(
            @PathVariable Long pedidoId
    ) {
        return ResponseEntity.ok(
                pedidoService.obtenerPorIdGestion(pedidoId)
        );
    }
}
