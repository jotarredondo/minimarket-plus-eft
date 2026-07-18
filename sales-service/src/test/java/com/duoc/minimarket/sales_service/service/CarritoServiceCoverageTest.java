package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.ActualizarCantidadItemRequest;
import com.duoc.minimarket.sales_service.dto.AgregarItemCarritoRequest;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import com.duoc.minimarket.sales_service.dto.CrearCarritoRequest;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.entity.ItemCarrito;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceCoverageTest {

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
    void debeRecuperarCarritoExistenteDeLaMismaSucursal() {
        Carrito carrito = crearCarrito(1L, 1L);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        CarritoResponse response =
                carritoService.crearORecuperar(
                        " CLIENTE@MINIMARKET.CL ",
                        new CrearCarritoRequest(1L)
                );

        assertEquals(1L, response.id());
        assertEquals(1L, response.sucursalId());

        verify(carritoRepository, never())
                .save(any(Carrito.class));
    }

    @Test
    void debeRechazarCarritoActivoEnOtraSucursal() {
        Carrito carrito = crearCarrito(1L, 2L);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        assertThrows(
                OperacionInvalidaException.class,
                () -> carritoService.crearORecuperar(
                        "cliente@minimarket.cl",
                        new CrearCarritoRequest(1L)
                )
        );
    }

    @Test
    void debeObtenerCarritoActivo() {
        Carrito carrito = crearCarrito(1L, 1L);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        CarritoResponse response =
                carritoService.obtenerActivo(
                        "CLIENTE@MINIMARKET.CL"
                );

        assertEquals(1L, response.id());
        assertEquals(EstadoCarrito.ACTIVO, response.estado());
    }

    @Test
    void debeInformarCuandoNoExisteCarritoActivo() {
        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> carritoService.obtenerActivo(
                        "cliente@minimarket.cl"
                )
        );
    }

    @Test
    void debeSumarCantidadCuandoProductoYaExiste() {
        Carrito carrito = crearCarrito(1L, 1L);
        ItemCarrito item = crearItem(carrito, 1L, 1);

        carrito.getItems().add(item);
        carrito.recalcularTotales();

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(1L, 1L, 10));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearProducto(true));

        when(
                itemCarritoRepository
                        .findByCarritoIdAndInventarioId(
                                1L,
                                1L
                        )
        ).thenReturn(Optional.of(item));

        when(
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1990.00"),
                        3
                )
        ).thenReturn(new BigDecimal("597.00"));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CarritoResponse response =
                carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(1L, 2),
                        "Bearer token"
                );

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).cantidad());

        assertEquals(
                0,
                response.total().compareTo(
                        new BigDecimal("5373.00")
                )
        );
    }

    @Test
    void debeActualizarCantidadYDescuento() {
        Carrito carrito = crearCarrito(1L, 1L);
        ItemCarrito item = crearItem(carrito, 1L, 1);

        carrito.getItems().add(item);
        carrito.recalcularTotales();

        when(
                itemCarritoRepository
                        .findByIdAndCarritoClienteEmail(
                                1L,
                                "cliente@minimarket.cl"
                        )
        ).thenReturn(Optional.of(item));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(1L, 1L, 20));

        when(
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1990.00"),
                        3
                )
        ).thenReturn(new BigDecimal("597.00"));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CarritoResponse response =
                carritoService.actualizarCantidad(
                        "cliente@minimarket.cl",
                        1L,
                        new ActualizarCantidadItemRequest(3),
                        "Bearer token"
                );

        assertEquals(3, response.items().get(0).cantidad());

        assertEquals(
                0,
                response.descuento().compareTo(
                        new BigDecimal("597.00")
                )
        );

        assertEquals(
                0,
                response.total().compareTo(
                        new BigDecimal("5373.00")
                )
        );
    }

    @Test
    void debeEliminarItemDelCarrito() {
        Carrito carrito = crearCarrito(1L, 1L);
        ItemCarrito item = crearItem(carrito, 1L, 2);

        carrito.getItems().add(item);
        carrito.recalcularTotales();

        when(
                itemCarritoRepository
                        .findByIdAndCarritoClienteEmail(
                                1L,
                                "cliente@minimarket.cl"
                        )
        ).thenReturn(Optional.of(item));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CarritoResponse response =
                carritoService.eliminarItem(
                        "cliente@minimarket.cl",
                        1L
                );

        assertEquals(0, response.items().size());

        assertEquals(
                0,
                response.total().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void debeVaciarCarritoCompleto() {
        Carrito carrito = crearCarrito(1L, 1L);

        ItemCarrito itemUno =
                crearItem(carrito, 1L, 1);

        ItemCarrito itemDos =
                crearItem(carrito, 2L, 2);

        itemDos.setProductoId(2L);
        itemDos.setInventarioId(2L);
        itemDos.setSku("BEB-002");

        carrito.getItems().add(itemUno);
        carrito.getItems().add(itemDos);
        carrito.recalcularTotales();

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(carritoRepository.save(any(Carrito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CarritoResponse response =
                carritoService.vaciar(
                        "cliente@minimarket.cl"
                );

        assertEquals(0, response.items().size());

        assertEquals(
                0,
                response.total().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void debeListarHistorialDeCarritos() {
        Carrito activo = crearCarrito(1L, 1L);

        Carrito convertido = crearCarrito(2L, 1L);
        convertido.setEstado(EstadoCarrito.CONVERTIDO);

        when(
                carritoRepository
                        .findByClienteEmailOrderByFechaCreacionDesc(
                                "cliente@minimarket.cl"
                        )
        ).thenReturn(List.of(convertido, activo));

        List<CarritoResponse> responses =
                carritoService.listarHistorial(
                        "cliente@minimarket.cl"
                );

        assertEquals(2, responses.size());
        assertEquals(
                EstadoCarrito.CONVERTIDO,
                responses.get(0).estado()
        );
    }

    @Test
    void debeRechazarAuthorizationHeaderInvalido() {
        assertThrows(
                OperacionInvalidaException.class,
                () -> carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(1L, 2),
                        null
                )
        );

        verify(carritoRepository, never())
                .findByClienteEmailAndEstado(
                        any(),
                        any()
                );
    }

    @Test
    void debeRechazarInventarioDeOtraSucursal() {
        Carrito carrito = crearCarrito(1L, 1L);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(1L, 2L, 20));

        assertThrows(
                OperacionInvalidaException.class,
                () -> carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(1L, 2),
                        "Bearer token"
                )
        );

        verify(catalogClient, never())
                .obtenerProducto(any(), any());
    }

    @Test
    void debeRechazarProductoInactivo() {
        Carrito carrito = crearCarrito(1L, 1L);

        when(
                carritoRepository.findByClienteEmailAndEstado(
                        "cliente@minimarket.cl",
                        EstadoCarrito.ACTIVO
                )
        ).thenReturn(Optional.of(carrito));

        when(
                catalogClient.obtenerInventario(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearInventario(1L, 1L, 20));

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(crearProducto(false));

        assertThrows(
                OperacionInvalidaException.class,
                () -> carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        new AgregarItemCarritoRequest(1L, 2),
                        "Bearer token"
                )
        );
    }

    private Carrito crearCarrito(
            Long id,
            Long sucursalId
    ) {
        return Carrito.builder()
                .id(id)
                .clienteEmail("cliente@minimarket.cl")
                .sucursalId(sucursalId)
                .estado(EstadoCarrito.ACTIVO)
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    private ItemCarrito crearItem(
            Carrito carrito,
            Long id,
            Integer cantidad
    ) {
        BigDecimal subtotal =
                new BigDecimal("1990.00")
                        .multiply(
                                BigDecimal.valueOf(cantidad)
                        );

        return ItemCarrito.builder()
                .id(id)
                .carrito(carrito)
                .productoId(1L)
                .inventarioId(1L)
                .sku("BEB-001")
                .nombreProducto("Bebida Cola 1.5 L")
                .precioUnitario(new BigDecimal("1990.00"))
                .cantidad(cantidad)
                .descuento(BigDecimal.ZERO)
                .subtotal(subtotal)
                .build();
    }

    private CatalogProductoResponse crearProducto(
            boolean activo
    ) {
        return new CatalogProductoResponse(
                1L,
                "BEB-001",
                "Bebida Cola 1.5 L",
                "Bebida de prueba",
                new BigDecimal("1990.00"),
                activo,
                1L,
                "Bebidas"
        );
    }

    private CatalogInventarioResponse crearInventario(
            Long productoId,
            Long sucursalId,
            Integer stock
    ) {
        return new CatalogInventarioResponse(
                1L,
                productoId,
                "BEB-001",
                "Bebida Cola 1.5 L",
                sucursalId,
                "SUC-001",
                "Sucursal Centro",
                stock,
                10,
                false,
                LocalDateTime.now()
        );
    }
}
