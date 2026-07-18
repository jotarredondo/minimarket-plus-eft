package com.duoc.minimarket.catalog_service.controller;

import com.duoc.minimarket.catalog_service.assembler.InventarioModelAssembler;
import com.duoc.minimarket.catalog_service.assembler.MovimientoInventarioModelAssembler;
import com.duoc.minimarket.catalog_service.config.OpenApiConfig;
import com.duoc.minimarket.catalog_service.dto.*;
import com.duoc.minimarket.catalog_service.service.InventarioService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventarios")
@Tag(
        name = "Inventarios",
        description = """
                Gestión del stock de productos por sucursal,
                movimientos de inventario y consulta de existencias.
                """
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioModelAssembler inventarioModelAssembler;
    private final MovimientoInventarioModelAssembler
            movimientoModelAssembler;

    public InventarioController(
            InventarioService inventarioService,
            InventarioModelAssembler inventarioModelAssembler,
            MovimientoInventarioModelAssembler
                    movimientoModelAssembler
    ) {
        this.inventarioService = inventarioService;
        this.inventarioModelAssembler =
                inventarioModelAssembler;
        this.movimientoModelAssembler =
                movimientoModelAssembler;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear un inventario",
            description = """
                    Registra el inventario inicial de un producto en una
                    sucursal. Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Inventario creado correctamente"
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
                    description = "Producto o sucursal no encontrados"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            Ya existe inventario para el producto
                            en la sucursal indicada
                            """
            )
    })
    public ResponseEntity<EntityModel<InventarioResponse>>
    crear(
            @Valid @RequestBody CrearInventarioRequest request,
            Authentication authentication
    ) {
        InventarioResponse inventario =
                inventarioService.crear(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inventarioModelAssembler.toModel(
                                inventario
                        )
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Obtener un inventario",
            description = """
                    Consulta un inventario por su identificador.
                    Incluye enlaces hacia producto, sucursal,
                    movimientos y órdenes de reposición.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario encontrado"
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
                    description = "Inventario no encontrado"
            )
    })
    public ResponseEntity<EntityModel<InventarioResponse>>
    obtenerPorId(
            @PathVariable Long id
    ) {
        InventarioResponse inventario =
                inventarioService.obtenerPorId(id);

        return ResponseEntity.ok(
                inventarioModelAssembler.toModel(inventario)
        );
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar inventarios por producto",
            description = """
                    Retorna la disponibilidad de un producto
                    en las distintas sucursales.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventarios obtenidos correctamente"
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
    public ResponseEntity<
            CollectionModel<EntityModel<InventarioResponse>>
            > listarPorProducto(
            @PathVariable Long productoId
    ) {
        List<InventarioResponse> inventarios =
                inventarioService.listarPorProducto(
                        productoId
                );

        return ResponseEntity.ok(
                inventarioModelAssembler
                        .toCollectionModelPorProducto(
                                inventarios,
                                productoId
                        )
        );
    }

    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar inventarios por sucursal",
            description = """
                    Retorna los productos y niveles de stock
                    disponibles en una sucursal.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventarios obtenidos correctamente"
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
    public ResponseEntity<
            CollectionModel<EntityModel<InventarioResponse>>
            > listarPorSucursal(
            @PathVariable Long sucursalId
    ) {
        List<InventarioResponse> inventarios =
                inventarioService.listarPorSucursal(
                        sucursalId
                );

        return ResponseEntity.ok(
                inventarioModelAssembler
                        .toCollectionModelPorSucursal(
                                inventarios,
                                sucursalId
                        )
        );
    }

    @PostMapping("/{inventarioId}/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Registrar un movimiento de inventario",
            description = """
                    Registra una entrada, salida o ajuste de stock.
                    Disponible solamente para usuarios ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Movimiento registrado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Movimiento inválido o stock insuficiente
                            """
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
    public ResponseEntity<
            EntityModel<MovimientoInventarioResponse>
            > registrarMovimiento(
            @PathVariable Long inventarioId,
            @Valid @RequestBody MovimientoInventarioRequest request,
            Authentication authentication
    ) {
        MovimientoInventarioResponse movimiento =
                inventarioService.registrarMovimiento(
                        inventarioId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        movimientoModelAssembler.toModel(
                                movimiento
                        )
                );
    }

    @GetMapping("/{inventarioId}/movimientos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'CAJERO', 'ADMIN')")
    @Operation(
            summary = "Listar movimientos de inventario",
            description = """
                    Retorna el historial de entradas, salidas
                    y ajustes asociados a un inventario.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimientos obtenidos correctamente"
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
                    description = "Inventario no encontrado"
            )
    })
    public ResponseEntity<
            CollectionModel<
                    EntityModel<MovimientoInventarioResponse>
                    >
            > listarMovimientos(
            @PathVariable Long inventarioId
    ) {
        List<MovimientoInventarioResponse> movimientos =
                inventarioService.listarMovimientos(
                        inventarioId
                );

        return ResponseEntity.ok(
                movimientoModelAssembler.toCollectionModel(
                        movimientos,
                        inventarioId
                )
        );
    }

    @PostMapping("/{inventarioId}/salidas-venta")
    @PreAuthorize("hasRole('CAJERO')")
    @Operation(
            summary = "Registrar salida de inventario por venta",
            description = """
                Descuenta stock cuando sales-service confirma una venta.
                La operación está disponible exclusivamente para usuarios
                autenticados con el rol CAJERO.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Salida de inventario registrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida o stock insuficiente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee el rol CAJERO"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado"
            )
    })
    public ResponseEntity<
            EntityModel<MovimientoInventarioResponse>
            > registrarSalidaVenta(
            @PathVariable Long inventarioId,
            @Valid @RequestBody SalidaVentaRequest request,
            Authentication authentication
    ) {
        MovimientoInventarioResponse movimiento =
                inventarioService.registrarSalidaVenta(
                        inventarioId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        movimientoModelAssembler.toModel(
                                movimiento
                        )
                );
    }
}
