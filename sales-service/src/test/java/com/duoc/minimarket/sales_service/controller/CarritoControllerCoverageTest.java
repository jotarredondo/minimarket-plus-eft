package com.duoc.minimarket.sales_service.controller;

import com.duoc.minimarket.sales_service.dto.ActualizarCantidadItemRequest;
import com.duoc.minimarket.sales_service.dto.AgregarItemCarritoRequest;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import com.duoc.minimarket.sales_service.dto.CrearCarritoRequest;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoControllerCoverageTest {

    @Mock
    private CarritoService carritoService;

    private CarritoController carritoController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        carritoController =
                new CarritoController(carritoService);

        authentication =
                new UsernamePasswordAuthenticationToken(
                        "cliente@minimarket.cl",
                        null,
                        List.of()
                );
    }

    @Test
    void debeCrearORecuperarCarrito() {
        CrearCarritoRequest request =
                new CrearCarritoRequest(1L);

        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.crearORecuperar(
                        "cliente@minimarket.cl",
                        request
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.crearORecuperar(
                        request,
                        authentication
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                EstadoCarrito.ACTIVO,
                response.getBody().estado()
        );

        verify(carritoService)
                .crearORecuperar(
                        "cliente@minimarket.cl",
                        request
                );
    }

    @Test
    void debeObtenerCarritoActivo() {
        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.obtenerActivo(
                        "cliente@minimarket.cl"
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.obtenerActivo(
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1L,
                response.getBody().id()
        );
    }

    @Test
    void debeAgregarProductoAlCarrito() {
        AgregarItemCarritoRequest request =
                new AgregarItemCarritoRequest(
                        1L,
                        2
                );

        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.agregarItem(
                        "cliente@minimarket.cl",
                        request,
                        "Bearer token"
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.agregarItem(
                        request,
                        authentication,
                        "Bearer token"
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        verify(carritoService)
                .agregarItem(
                        "cliente@minimarket.cl",
                        request,
                        "Bearer token"
                );
    }

    @Test
    void debeActualizarCantidadDeItem() {
        ActualizarCantidadItemRequest request =
                new ActualizarCantidadItemRequest(3);

        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.actualizarCantidad(
                        "cliente@minimarket.cl",
                        1L,
                        request,
                        "Bearer token"
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.actualizarCantidad(
                        1L,
                        request,
                        authentication,
                        "Bearer token"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        verify(carritoService)
                .actualizarCantidad(
                        "cliente@minimarket.cl",
                        1L,
                        request,
                        "Bearer token"
                );
    }

    @Test
    void debeEliminarItemDelCarrito() {
        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.eliminarItem(
                        "cliente@minimarket.cl",
                        1L
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.eliminarItem(
                        1L,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        verify(carritoService)
                .eliminarItem(
                        "cliente@minimarket.cl",
                        1L
                );
    }

    @Test
    void debeVaciarCarrito() {
        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.vaciar(
                        "cliente@minimarket.cl"
                )
        ).thenReturn(carrito);

        ResponseEntity<CarritoResponse> response =
                carritoController.vaciar(
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        verify(carritoService)
                .vaciar(
                        "cliente@minimarket.cl"
                );
    }

    @Test
    void debeListarHistorialDeCarritos() {
        CarritoResponse carrito =
                crearCarritoResponse();

        when(
                carritoService.listarHistorial(
                        "cliente@minimarket.cl"
                )
        ).thenReturn(List.of(carrito));

        ResponseEntity<List<CarritoResponse>> response =
                carritoController.listarHistorial(
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1,
                response.getBody().size()
        );

        verify(carritoService)
                .listarHistorial(
                        "cliente@minimarket.cl"
                );
    }

    private CarritoResponse crearCarritoResponse() {
        return new CarritoResponse(
                1L,
                "cliente@minimarket.cl",
                1L,
                EstadoCarrito.ACTIVO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
    }
}
