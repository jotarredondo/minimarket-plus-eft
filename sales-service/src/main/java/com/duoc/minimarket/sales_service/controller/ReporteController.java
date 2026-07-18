package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.ReporteRotacionResponse;
import com.duoc.minimarket.sales_service.dto.ResumenVentasResponse;
import com.duoc.minimarket.sales_service.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(
            ReporteService reporteService
    ) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen-ventas")
    public ResponseEntity<ResumenVentasResponse>
    obtenerResumenVentas() {
        return ResponseEntity.ok(
                reporteService.obtenerResumenVentas()
        );
    }

    @GetMapping("/productos-rotacion")
    public ResponseEntity<ReporteRotacionResponse>
    obtenerRotacionProductos() {
        return ResponseEntity.ok(
                reporteService.obtenerRotacionProductos()
        );
    }
}
