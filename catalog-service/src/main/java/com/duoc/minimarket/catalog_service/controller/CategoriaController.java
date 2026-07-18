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


import com.duoc.minimarket.catalog_service.assembler.CategoriaModelAssembler;
import com.duoc.minimarket.catalog_service.config.OpenApiConfig;
import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(
        name = "Categorías",
        description = """
                Gestión de las categorías utilizadas para clasificar
                los productos disponibles en MiniMarket Plus.
                """
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaModelAssembler categoriaModelAssembler;

    public CategoriaController(
            CategoriaService categoriaService,
            CategoriaModelAssembler categoriaModelAssembler
    ) {
        this.categoriaService = categoriaService;
        this.categoriaModelAssembler = categoriaModelAssembler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar categorías activas",
            description = """
                    Retorna las categorías activas del catálogo.
                    Cada categoría incluye enlaces hacia su detalle
                    y hacia los productos que pertenecen a ella.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías obtenidas correctamente"
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
            CollectionModel<EntityModel<CategoriaResponse>>
            > listar() {

        List<CategoriaResponse> categorias =
                categoriaService.listarActivas();

        return ResponseEntity.ok(
                categoriaModelAssembler.toCollectionModel(
                        categorias
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Obtener una categoría",
            description = """
                    Busca una categoría mediante su identificador
                    e incorpora un enlace hacia sus productos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada"
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
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<EntityModel<CategoriaResponse>>
    obtenerPorId(
            @PathVariable Long id
    ) {
        CategoriaResponse categoria =
                categoriaService.obtenerPorId(id);

        return ResponseEntity.ok(
                categoriaModelAssembler.toModel(categoria)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear una categoría",
            description = """
                    Registra una nueva categoría.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada correctamente"
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
                    description = "Ya existe una categoría con ese nombre"
            )
    })
    public ResponseEntity<EntityModel<CategoriaResponse>>
    crear(
            @Valid @RequestBody CategoriaRequest request
    ) {
        CategoriaResponse categoria =
                categoriaService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        categoriaModelAssembler.toModel(categoria)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar una categoría",
            description = """
                    Modifica una categoría existente.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente"
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
                    description = "Categoría no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nombre pertenece a otra categoría"
            )
    })
    public ResponseEntity<EntityModel<CategoriaResponse>>
    actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        CategoriaResponse categoria =
                categoriaService.actualizar(id, request);

        return ResponseEntity.ok(
                categoriaModelAssembler.toModel(categoria)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cambiar estado de una categoría",
            description = """
                    Activa o desactiva lógicamente una categoría.
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
                    description = "Categoría no encontrada"
            )
    })
    public ResponseEntity<EntityModel<CategoriaResponse>>
    cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        CategoriaResponse categoria =
                categoriaService.cambiarEstado(id, activo);

        return ResponseEntity.ok(
                categoriaModelAssembler.toModel(categoria)
        );
    }
}
