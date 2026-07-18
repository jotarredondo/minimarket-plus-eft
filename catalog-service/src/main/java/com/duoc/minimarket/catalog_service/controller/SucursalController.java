package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.SucursalModelAssembler;
import com.duoc.minimarket.catalog_service.config.OpenApiConfig;
import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@Tag(
        name = "Sucursales",
        description = """
                Gestión de las sucursales de MiniMarket Plus.
                Permite consultar y administrar sus datos,
                además de navegar hacia el inventario asociado.
                """
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class SucursalController {

    private final SucursalService sucursalService;
    private final SucursalModelAssembler sucursalModelAssembler;

    public SucursalController(
            SucursalService sucursalService,
            SucursalModelAssembler sucursalModelAssembler
    ) {
        this.sucursalService = sucursalService;
        this.sucursalModelAssembler = sucursalModelAssembler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar sucursales activas",
            description = """
                    Retorna todas las sucursales activas.
                    Cada sucursal incorpora enlaces hacia su detalle
                    y hacia los inventarios almacenados en ella.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sucursales obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol requerido"
            )
    })
    public ResponseEntity<
            CollectionModel<EntityModel<SucursalResponse>>
            > listar() {

        List<SucursalResponse> sucursales =
                sucursalService.listarActivas();

        return ResponseEntity.ok(
                sucursalModelAssembler.toCollectionModel(
                        sucursales
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Obtener una sucursal",
            description = """
                    Busca una sucursal por su identificador.
                    La respuesta incorpora un enlace hacia su inventario.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sucursal encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol requerido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sucursal no encontrada"
            )
    })
    public ResponseEntity<EntityModel<SucursalResponse>>
    obtenerPorId(
            @PathVariable Long id
    ) {
        SucursalResponse sucursal =
                sucursalService.obtenerPorId(id);

        return ResponseEntity.ok(
                sucursalModelAssembler.toModel(sucursal)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear una sucursal",
            description = """
                    Registra una nueva sucursal.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sucursal creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
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
                    responseCode = "409",
                    description = "Ya existe una sucursal con ese código"
            )
    })
    public ResponseEntity<EntityModel<SucursalResponse>>
    crear(
            @Valid @RequestBody SucursalRequest request
    ) {
        SucursalResponse sucursal =
                sucursalService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sucursalModelAssembler.toModel(sucursal)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar una sucursal",
            description = """
                    Modifica los datos de una sucursal existente.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sucursal actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
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
                    description = "Sucursal no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El código pertenece a otra sucursal"
            )
    })
    public ResponseEntity<EntityModel<SucursalResponse>>
    actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequest request
    ) {
        SucursalResponse sucursal =
                sucursalService.actualizar(id, request);

        return ResponseEntity.ok(
                sucursalModelAssembler.toModel(sucursal)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cambiar estado de una sucursal",
            description = """
                    Activa o desactiva lógicamente una sucursal.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente"
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
                    description = "Sucursal no encontrada"
            )
    })
    public ResponseEntity<EntityModel<SucursalResponse>>
    cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        SucursalResponse sucursal =
                sucursalService.cambiarEstado(id, activo);

        return ResponseEntity.ok(
                sucursalModelAssembler.toModel(sucursal)
        );
    }
}