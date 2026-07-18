package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.ProductoModelAssembler;
import com.duoc.minimarket.catalog_service.config.OpenApiConfig;
import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.service.ProductoService;
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
@RequestMapping("/api/productos")
@Tag(
        name = "Productos",
        description = """
                Gestión del catálogo de productos de MiniMarket Plus.
                Permite consultar productos y administrar su información.
                """
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoModelAssembler productoModelAssembler;

    public ProductoController(
            ProductoService productoService,
            ProductoModelAssembler productoModelAssembler
    ) {
        this.productoService = productoService;
        this.productoModelAssembler = productoModelAssembler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar productos activos",
            description = """
                    Retorna todos los productos activos. Opcionalmente,
                    permite filtrar los productos por categoría.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Productos obtenidos correctamente"
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
            CollectionModel<EntityModel<ProductoResponse>>
            > listar(
            @RequestParam(required = false) Long categoriaId
    ) {
        List<ProductoResponse> productos;

        if (categoriaId != null) {
            productos =
                    productoService.listarPorCategoria(categoriaId);
        } else {
            productos =
                    productoService.listarActivos();
        }

        return ResponseEntity.ok(
                productoModelAssembler.toCollectionModel(productos)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Obtener un producto",
            description = """
                    Busca un producto mediante su identificador e incorpora
                    enlaces hacia su categoría, inventarios y catálogo.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado"
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
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<EntityModel<ProductoResponse>>
    obtenerPorId(
            @PathVariable Long id
    ) {
        ProductoResponse producto =
                productoService.obtenerPorId(id);

        return ResponseEntity.ok(
                productoModelAssembler.toModel(producto)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear un producto",
            description = """
                    Registra un producto nuevo asociado a una categoría.
                    Esta operación está permitida solamente para ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente"
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
                    description = "Ya existe un producto con el mismo SKU"
            )
    })
    public ResponseEntity<EntityModel<ProductoResponse>>
    crear(
            @Valid @RequestBody ProductoRequest request
    ) {
        ProductoResponse producto =
                productoService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productoModelAssembler.toModel(producto)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar un producto",
            description = """
                    Modifica los datos de un producto existente.
                    Esta operación está permitida solamente para ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado correctamente"
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
                    description = "Producto o categoría no encontrados"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El SKU pertenece a otro producto"
            )
    })
    public ResponseEntity<EntityModel<ProductoResponse>>
    actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        ProductoResponse producto =
                productoService.actualizar(id, request);

        return ResponseEntity.ok(
                productoModelAssembler.toModel(producto)
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cambiar estado de un producto",
            description = """
                    Activa o desactiva lógicamente un producto.
                    Esta operación está permitida solamente para ADMIN.
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
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<EntityModel<ProductoResponse>>
    cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        ProductoResponse producto =
                productoService.cambiarEstado(id, activo);

        return ResponseEntity.ok(
                productoModelAssembler.toModel(producto)
        );
    }
}