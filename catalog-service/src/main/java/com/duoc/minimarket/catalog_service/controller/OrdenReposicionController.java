package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.config.OpenApiConfig;
import com.duoc.minimarket.catalog_service.dto.ActualizarOrdenReposicionRequest;
import com.duoc.minimarket.catalog_service.dto.OrdenReposicionResponse;
import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-reposicion")
@Tag(
        name = "Órdenes de reposición",
        description = """
                Consulta y administración de órdenes generadas
                automáticamente cuando el inventario alcanza
                su nivel mínimo de stock.
                """
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class OrdenReposicionController {

    private final InventarioService inventarioService;

    public OrdenReposicionController(
            InventarioService inventarioService
    ) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar órdenes por estado",
            description = """
                    Retorna las órdenes de reposición que coinciden
                    con el estado indicado: GENERADA, PROCESADA
                    o CANCELADA.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol ADMIN"
            )
    })
    public ResponseEntity<List<OrdenReposicionResponse>>
    listarPorEstado(
            @PathVariable EstadoOrdenReposicion estado
    ) {
        return ResponseEntity.ok(
                inventarioService.listarOrdenesPorEstado(estado)
        );
    }

    @GetMapping("/inventario/{inventarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar órdenes de un inventario",
            description = """
                    Retorna el historial de órdenes de reposición
                    asociadas a un inventario específico.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Órdenes obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado"
            )
    })
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar estado de una orden",
            description = """
                    Permite marcar una orden como PROCESADA
                    o CANCELADA. Disponible exclusivamente
                    para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cambio de estado inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orden de reposición no encontrada"
            )
    })
    public ResponseEntity<OrdenReposicionResponse>
    actualizarEstado(
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