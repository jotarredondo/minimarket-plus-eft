package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.AgregarItemCarritoRequest;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import com.duoc.minimarket.sales_service.dto.CrearCarritoRequest;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
import com.duoc.minimarket.sales_service.repository.CarritoRepository;
import com.duoc.minimarket.sales_service.repository.ItemCarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private PromocionService promocionService;

    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoService(
                carritoRepository,
                itemCarritoRepository,
                catalogClient,
                promocionService
        );
    }

    @Test
    void debeCrearCarritoNuevo() {
        CrearCarritoRequest request =
                new CrearCarritoRequest(1L);

        when(
                carritoRepository
                        .findByClienteEmailAndEstado(
                                "cliente@minimarket.cl",
                                EstadoCarrito.ACTIVO
                        )
        ).thenReturn(Optional.empty());

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation -> {
                    Carrito carrito =
                            invocation.getArgument(0);

                    carrito.setId(1L);
                    return carrito;
                });

        CarritoResponse response =
                carritoService.crearORecuperar(
                        "CLIENTE@MINIMARKET.CL",
                        request
                );

        assertEquals(1L, response.id());
        assertEquals(
                "cliente@minimarket.cl",
                response.clienteEmail()
        );
        assertEquals(1L, response.sucursalId());
        assertEquals(
                EstadoCarrito.ACTIVO,
                response.estado()
        );
        assertEquals(0, response.total()
                .compareTo(BigDecimal.ZERO));
    }

    @Test
    void debeAgregarProductoConPromocion() {
        Carrito carrito = crearCarrito();

        CatalogInventarioResponse inventario =
                crearInventario(20);

        CatalogProductoResponse producto =
                crearProducto();

        when(
                carritoRepository
                        .findByClienteEmailAndEstado(
                                "cliente@minimarket.cl",
                                EstadoCarrito.ACTIVO
                        )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(inventario);

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(producto);

        when(
                itemCarritoRepository
                        .findByCarritoIdAndInventarioId(
                                1L,
                                1L
                        )
        ).thenReturn(Optional.empty());

        when(
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1990.00"),
                        2
                )
        ).thenReturn(new BigDecimal("398.00"));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CarritoResponse response =
                carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(
                                1L,
                                2
                        ),
                        "Bearer token-cliente"
                );

        assertEquals(1, response.items().size());
        assertEquals(
                2,
                response.items().get(0).cantidad()
        );

        assertEquals(
                0,
                response.descuento().compareTo(
                        new BigDecimal("398.00")
                )
        );

        assertEquals(
                0,
                response.total().compareTo(
                        new BigDecimal("3582.00")
                )
        );

        verify(promocionService)
                .calcularMejorDescuento(
                        1L,
                        new BigDecimal("1990.00"),
                        2
                );
    }

    @Test
    void debeRechazarCantidadSuperiorAlStock() {
        Carrito carrito = crearCarrito();

        when(
                carritoRepository
                        .findByClienteEmailAndEstado(
                                "cliente@minimarket.cl",
                                EstadoCarrito.ACTIVO
                        )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(crearInventario(5));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token-cliente"
                )
        ).thenReturn(crearProducto());

        when(
                itemCarritoRepository
                        .findByCarritoIdAndInventarioId(
                                1L,
                                1L
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                StockInsuficienteException.class,
                () -> carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(
                                1L,
                                10
                        ),
                        "Bearer token-cliente"
                )
        );
    }

    private Carrito crearCarrito() {
        return Carrito.builder()
                .id(1L)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(1L)
                .estado(EstadoCarrito.ACTIVO)
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    private CatalogProductoResponse crearProducto() {
        return new CatalogProductoResponse(
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida de prueba",
                new BigDecimal("1990.00"),
                true,
                1L,
                "Bebidas"
        );
    }

    private CatalogInventarioResponse crearInventario(
            Integer stock
    ) {
        return new CatalogInventarioResponse(
                1L,
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                1L,
                "SUC-001",
                "Sucursal Centro",
                stock,
                10,
                false,
                LocalDateTime.now()
        );
    }
}