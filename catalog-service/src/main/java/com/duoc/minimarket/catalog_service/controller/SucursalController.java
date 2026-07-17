package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.service.SucursalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<List<SucursalResponse>> listar() {
        return ResponseEntity.ok(
                sucursalService.listarActivas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<SucursalResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                sucursalService.obtenerPorId(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponse> crear(
            @Valid @RequestBody SucursalRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sucursalService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequest request
    ) {
        return ResponseEntity.ok(
                sucursalService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                sucursalService.cambiarEstado(id, activo)
        );
    }
}
