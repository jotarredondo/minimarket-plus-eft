package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.assembler.CarritoModelAssembler;
import com.duoc.minimarket.sales_service.dto.ActualizarCantidadItemRequest;
import com.duoc.minimarket.sales_service.dto.AgregarItemCarritoRequest;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import com.duoc.minimarket.sales_service.dto.CrearCarritoRequest;
import com.duoc.minimarket.sales_service.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carritos")
@Tag(
        name = "Carritos",
        description = "Gestión del carrito de compras del cliente"
)
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoModelAssembler carritoModelAssembler;

    /*
     * Se conserva el constructor original para no romper
     * las pruebas que crean el controlador directamente.
     */
    public CarritoController(
            CarritoService carritoService
    ) {
        this.carritoService = carritoService;
        this.carritoModelAssembler =
                new CarritoModelAssembler();
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Crear o recuperar carrito",
            description = """
                    Crea un carrito activo para el cliente o recupera
                    el carrito existente en la misma sucursal.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Carrito creado o recuperado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de carrito inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol CLIENTE"
            )
    })
    public ResponseEntity<CarritoResponse> crearORecuperar(
            @Valid @RequestBody
            CrearCarritoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        carritoService.crearORecuperar(
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping("/actual")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Obtener carrito activo"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito activo encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cliente no tiene carrito activo"
            )
    })
    public ResponseEntity<CarritoResponse> obtenerActivo(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                carritoService.obtenerActivo(
                        authentication.getName()
                )
        );
    }

    /*
     * Endpoint específico para evidenciar HATEOAS
     * sin modificar los endpoints existentes.
     */
    @GetMapping("/actual/hateoas")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Obtener carrito activo con HATEOAS",
            description = """
                    Retorna el carrito activo con enlaces dinámicos
                    para navegar por la API.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito con enlaces HATEOAS"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol CLIENTE"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cliente no tiene carrito activo"
            )
    })
    public ResponseEntity<EntityModel<CarritoResponse>>
    obtenerActivoHateoas(
            Authentication authentication
    ) {
        CarritoResponse carrito =
                carritoService.obtenerActivo(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                carritoModelAssembler.toModel(carrito)
        );
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Agregar producto al carrito",
            description = """
                    Agrega un producto y valida inventario,
                    sucursal, stock y promociones vigentes.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto agregado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Stock insuficiente o datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Catalog Service no disponible"
            )
    })
    public ResponseEntity<CarritoResponse> agregarItem(
            @Valid @RequestBody
            AgregarItemCarritoRequest request,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        carritoService.agregarItem(
                                authentication.getName(),
                                request,
                                authorizationHeader
                        )
                );
    }

    @PatchMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Actualizar cantidad de producto"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cantidad actualizada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida o stock insuficiente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ítem no encontrado"
            )
    })
    public ResponseEntity<CarritoResponse>
    actualizarCantidad(
            @PathVariable Long itemId,
            @Valid @RequestBody
            ActualizarCantidadItemRequest request,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity.ok(
                carritoService.actualizarCantidad(
                        authentication.getName(),
                        itemId,
                        request,
                        authorizationHeader
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Eliminar producto del carrito"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto eliminado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ítem no encontrado"
            )
    })
    public ResponseEntity<CarritoResponse> eliminarItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                carritoService.eliminarItem(
                        authentication.getName(),
                        itemId
                )
        );
    }

    @DeleteMapping("/items")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Vaciar carrito"
    )
    public ResponseEntity<CarritoResponse> vaciar(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                carritoService.vaciar(
                        authentication.getName()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Listar historial de carritos"
    )
    public ResponseEntity<List<CarritoResponse>>
    listarHistorial(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                carritoService.listarHistorial(
                        authentication.getName()
                )
        );
    }
}