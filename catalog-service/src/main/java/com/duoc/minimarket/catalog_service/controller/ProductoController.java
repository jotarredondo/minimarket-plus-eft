package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) Long categoriaId
    ) {
        if (categoriaId != null) {
            return ResponseEntity.ok(
                    productoService.listarPorCategoria(categoriaId)
            );
        }

        return ResponseEntity.ok(
                productoService.listarActivos()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                productoService.obtenerPorId(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity.ok(
                productoService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                productoService.cambiarEstado(id, activo)
        );
    }
}