package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.PromocionRequest;
import com.duoc.minimarket.sales_service.dto.PromocionResponse;
import com.duoc.minimarket.sales_service.service.PromocionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(
            PromocionService promocionService
    ) {
        this.promocionService = promocionService;
    }

    @GetMapping("/activas")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')"
    )
    public ResponseEntity<List<PromocionResponse>>
    listarActivas() {
        return ResponseEntity.ok(
                promocionService.listarActivas()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionResponse> crear(
            @Valid @RequestBody PromocionRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        promocionService.crear(
                                request,
                                authorizationHeader
                        )
                );
    }

    @PatchMapping("/{promocionId}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromocionResponse>
    cambiarEstado(
            @PathVariable Long promocionId,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                promocionService.cambiarEstado(
                        promocionId,
                        activo
                )
        );
    }
}
