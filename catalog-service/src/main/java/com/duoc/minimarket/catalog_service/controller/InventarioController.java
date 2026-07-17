package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.CrearInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.InventarioResponse;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioResponse;
import com.duoc.minimarket.catalog_service.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventarioResponse> crear(
            @Valid @RequestBody CrearInventarioRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inventarioService.crear(
                                request,
                                authentication.getName()
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<InventarioResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                inventarioService.obtenerPorId(id)
        );
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<List<InventarioResponse>> listarPorProducto(
            @PathVariable Long productoId
    ) {
        return ResponseEntity.ok(
                inventarioService.listarPorProducto(productoId)
        );
    }

    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<List<InventarioResponse>> listarPorSucursal(
            @PathVariable Long sucursalId
    ) {
        return ResponseEntity.ok(
                inventarioService.listarPorSucursal(sucursalId)
        );
    }

    @PostMapping("/{id}/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimientoInventarioResponse> registrarMovimiento(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoInventarioRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inventarioService.registrarMovimiento(
                                id,
                                request,
                                authentication.getName()
                        )
                );
    }

    @GetMapping("/{id}/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MovimientoInventarioResponse>> listarMovimientos(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                inventarioService.listarMovimientos(id)
        );
    }
}
