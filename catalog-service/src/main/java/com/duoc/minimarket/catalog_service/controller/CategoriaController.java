package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(
                categoriaService.listarActivas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    public ResponseEntity<CategoriaResponse> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                categoriaService.obtenerPorId(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> crear(
            @Valid @RequestBody CategoriaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        return ResponseEntity.ok(
                categoriaService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                categoriaService.cambiarEstado(id, activo)
        );
    }
}
