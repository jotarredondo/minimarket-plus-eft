package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.VentaResponse;
import com.duoc.minimarket.sales_service.service.VentaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(
            VentaService ventaService
    ) {
        this.ventaService = ventaService;
    }

    @PostMapping("/pedidos/{pedidoId}/confirmar")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<VentaResponse> confirmarVenta(
            @PathVariable Long pedidoId,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ventaService.confirmarVenta(
                                pedidoId,
                                authentication.getName(),
                                authorizationHeader
                        )
                );
    }

    @GetMapping("/mis-ventas")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<List<VentaResponse>>
    listarMisVentas(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ventaService.listarPorCajero(
                        authentication.getName()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VentaResponse>>
    listarTodas() {
        return ResponseEntity.ok(
                ventaService.listarTodas()
        );
    }

    @GetMapping("/{ventaId}")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    public ResponseEntity<VentaResponse>
    obtenerPorId(
            @PathVariable Long ventaId
    ) {
        return ResponseEntity.ok(
                ventaService.obtenerPorId(ventaId)
        );
    }
}
