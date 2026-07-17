package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.ActualizarOrdenReposicionRequest;
import com.duoc.minimarket.catalog_service.dto.OrdenReposicionResponse;
import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-reposicion")
@PreAuthorize("hasRole('ADMIN')")
public class OrdenReposicionController {

    private final InventarioService inventarioService;

    public OrdenReposicionController(
            InventarioService inventarioService
    ) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<OrdenReposicionResponse>> listarPorEstado(
            @RequestParam(
                    defaultValue = "GENERADA"
            ) EstadoOrdenReposicion estado
    ) {
        return ResponseEntity.ok(
                inventarioService.listarOrdenesPorEstado(estado)
        );
    }

    @GetMapping("/inventario/{inventarioId}")
    public ResponseEntity<List<OrdenReposicionResponse>>
    listarPorInventario(
            @PathVariable Long inventarioId
    ) {
        return ResponseEntity.ok(
                inventarioService.listarOrdenesPorInventario(
                        inventarioId
                )
        );
    }

    @PatchMapping("/{ordenId}/estado")
    public ResponseEntity<OrdenReposicionResponse> actualizarEstado(
            @PathVariable Long ordenId,
            @Valid @RequestBody
            ActualizarOrdenReposicionRequest request
    ) {
        return ResponseEntity.ok(
                inventarioService.actualizarEstadoOrden(
                        ordenId,
                        request
                )
        );
    }
}