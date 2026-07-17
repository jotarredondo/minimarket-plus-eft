package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.CrearInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.InventarioResponse;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioRequest;
import com.duoc.minimarket.catalog_service.dto.MovimientoInventarioResponse;
import com.duoc.minimarket.catalog_service.entity.Categoria;
import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.entity.Inventario;
import com.duoc.minimarket.catalog_service.entity.MovimientoInventario;
import com.duoc.minimarket.catalog_service.entity.OrdenReposicion;
import com.duoc.minimarket.catalog_service.entity.Producto;
import com.duoc.minimarket.catalog_service.entity.Sucursal;
import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;
import com.duoc.minimarket.catalog_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.StockInsuficienteException;
import com.duoc.minimarket.catalog_service.repository.InventarioRepository;
import com.duoc.minimarket.catalog_service.repository.MovimientoInventarioRepository;
import com.duoc.minimarket.catalog_service.repository.OrdenReposicionRepository;
import com.duoc.minimarket.catalog_service.repository.ProductoRepository;
import com.duoc.minimarket.catalog_service.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    private static final String ADMIN_EMAIL =
            "admin@minimarket.cl";

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private OrdenReposicionRepository ordenReposicionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Producto producto;
    private Sucursal sucursal;
    private Inventario inventario;

    @BeforeEach
    void configurarDatos() {
        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Bebidas")
                .descripcion("Bebidas y refrescos")
                .activo(true)
                .build();

        producto = Producto.builder()
                .id(1L)
                .sku("BEB-001")
                .nombre("Bebida Cola 1.5 L")
                .descripcion("Bebida gaseosa")
                .precio(new BigDecimal("1990.00"))
                .activo(true)
                .categoria(categoria)
                .build();

        sucursal = Sucursal.builder()
                .id(1L)
                .codigo("SUC-001")
                .nombre("Sucursal Centro")
                .direccion("Avenida Principal 100")
                .activo(true)
                .build();

        inventario = Inventario.builder()
                .id(1L)
                .producto(producto)
                .sucursal(sucursal)
                .stockActual(20)
                .stockMinimo(10)
                .fechaActualizacion(
                        LocalDateTime.of(2026, 7, 17, 10, 0)
                )
                .build();
    }

    @Test
    void crear_debeCrearInventarioYMovimientoInicial() {
        CrearInventarioRequest request =
                new CrearInventarioRequest(
                        1L,
                        1L,
                        20,
                        10
                );

        when(inventarioRepository
                .existsByProductoIdAndSucursalId(1L, 1L))
                .thenReturn(false);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        when(inventarioRepository.save(any(Inventario.class)))
                .thenAnswer(invocation -> {
                    Inventario guardado = invocation.getArgument(0);
                    guardado.setId(1L);
                    guardado.setFechaActualizacion(
                            LocalDateTime.of(2026, 7, 17, 10, 0)
                    );
                    return guardado;
                });

        when(movimientoRepository.save(
                any(MovimientoInventario.class)
        )).thenAnswer(invocation -> {
            MovimientoInventario movimiento =
                    invocation.getArgument(0);

            movimiento.setId(1L);
            movimiento.setFechaMovimiento(
                    LocalDateTime.of(2026, 7, 17, 10, 0)
            );

            return movimiento;
        });

        InventarioResponse response =
                inventarioService.crear(
                        request,
                        ADMIN_EMAIL
                );

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals(1L, response.productoId()),
                () -> assertEquals("BEB-001", response.productoSku()),
                () -> assertEquals(1L, response.sucursalId()),
                () -> assertEquals(20, response.stockActual()),
                () -> assertEquals(10, response.stockMinimo()),
                () -> assertFalse(response.requiereReposicion())
        );

        verify(movimientoRepository)
                .save(any(MovimientoInventario.class));

        verify(ordenReposicionRepository, never())
                .save(any(OrdenReposicion.class));
    }

    @Test
    void crear_debeLanzarExcepcionCuandoInventarioYaExiste() {
        CrearInventarioRequest request =
                new CrearInventarioRequest(
                        1L,
                        1L,
                        20,
                        10
                );

        when(inventarioRepository
                .existsByProductoIdAndSucursalId(1L, 1L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> inventarioService.crear(
                        request,
                        ADMIN_EMAIL
                )
        );

        verify(productoRepository, never()).findById(any());
        verify(sucursalRepository, never()).findById(any());
        verify(inventarioRepository, never())
                .save(any(Inventario.class));
    }

    @Test
    void registrarMovimiento_debeRegistrarEntrada() {
        inventario.setStockActual(8);

        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.ENTRADA,
                        10,
                        "Recepción de mercadería"
                );

        prepararMovimiento();

        MovimientoInventarioResponse response =
                inventarioService.registrarMovimiento(
                        1L,
                        request,
                        ADMIN_EMAIL
                );

        assertAll(
                () -> assertEquals(
                        TipoMovimientoInventario.ENTRADA,
                        response.tipo()
                ),
                () -> assertEquals(8, response.stockAnterior()),
                () -> assertEquals(18, response.stockPosterior()),
                () -> assertEquals(
                        ADMIN_EMAIL,
                        response.usuarioEmail()
                )
        );

        assertEquals(18, inventario.getStockActual());

        verify(ordenReposicionRepository, never())
                .save(any(OrdenReposicion.class));
    }

    @Test
    void registrarMovimiento_debeGenerarOrdenCuandoStockQuedaBajoMinimo() {
        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        12,
                        "Salida por venta"
                );

        prepararMovimiento();

        when(ordenReposicionRepository
                .existsByInventarioIdAndEstado(
                        1L,
                        EstadoOrdenReposicion.GENERADA
                ))
                .thenReturn(false);

        when(ordenReposicionRepository.save(
                any(OrdenReposicion.class)
        )).thenAnswer(invocation -> {
            OrdenReposicion orden = invocation.getArgument(0);
            orden.setId(1L);
            orden.setFechaGeneracion(
                    LocalDateTime.of(2026, 7, 17, 10, 5)
            );
            return orden;
        });

        MovimientoInventarioResponse response =
                inventarioService.registrarMovimiento(
                        1L,
                        request,
                        ADMIN_EMAIL
                );

        assertAll(
                () -> assertEquals(20, response.stockAnterior()),
                () -> assertEquals(8, response.stockPosterior()),
                () -> assertEquals(8, inventario.getStockActual())
        );

        ArgumentCaptor<OrdenReposicion> captor =
                ArgumentCaptor.forClass(OrdenReposicion.class);

        verify(ordenReposicionRepository).save(captor.capture());

        OrdenReposicion orden = captor.getValue();

        assertAll(
                () -> assertEquals(
                        EstadoOrdenReposicion.GENERADA,
                        orden.getEstado()
                ),
                () -> assertEquals(
                        12,
                        orden.getCantidadSugerida()
                ),
                () -> assertEquals(
                        inventario,
                        orden.getInventario()
                )
        );
    }

    @Test
    void registrarMovimiento_noDebeCrearOrdenDuplicada() {
        inventario.setStockActual(9);

        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        1,
                        "Nueva salida"
                );

        prepararMovimiento();

        when(ordenReposicionRepository
                .existsByInventarioIdAndEstado(
                        1L,
                        EstadoOrdenReposicion.GENERADA
                ))
                .thenReturn(true);

        inventarioService.registrarMovimiento(
                1L,
                request,
                ADMIN_EMAIL
        );

        assertEquals(8, inventario.getStockActual());

        verify(ordenReposicionRepository, never())
                .save(any(OrdenReposicion.class));
    }

    @Test
    void registrarMovimiento_debeRechazarSalidaConStockInsuficiente() {
        inventario.setStockActual(8);

        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        20,
                        "Prueba de stock insuficiente"
                );

        when(inventarioRepository.findById(1L))
                .thenReturn(Optional.of(inventario));

        assertThrows(
                StockInsuficienteException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        request,
                        ADMIN_EMAIL
                )
        );

        assertEquals(8, inventario.getStockActual());

        verify(inventarioRepository, never())
                .save(any(Inventario.class));

        verify(movimientoRepository, never())
                .save(any(MovimientoInventario.class));

        verify(ordenReposicionRepository, never())
                .save(any(OrdenReposicion.class));
    }

    @Test
    void registrarMovimiento_debeAjustarStockAlValorExacto() {
        inventario.setStockActual(8);

        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.AJUSTE,
                        25,
                        "Corrección por conteo físico"
                );

        prepararMovimiento();

        MovimientoInventarioResponse response =
                inventarioService.registrarMovimiento(
                        1L,
                        request,
                        ADMIN_EMAIL
                );

        assertAll(
                () -> assertEquals(8, response.stockAnterior()),
                () -> assertEquals(25, response.stockPosterior()),
                () -> assertEquals(25, inventario.getStockActual())
        );
    }

    @Test
    void registrarMovimiento_debeRechazarEntradaCero() {
        MovimientoInventarioRequest request =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.ENTRADA,
                        0,
                        "Entrada inválida"
                );

        when(inventarioRepository.findById(1L))
                .thenReturn(Optional.of(inventario));

        assertThrows(
                OperacionInvalidaException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        request,
                        ADMIN_EMAIL
                )
        );

        verify(inventarioRepository, never())
                .save(any(Inventario.class));
    }

    private void prepararMovimiento() {
        when(inventarioRepository.findById(1L))
                .thenReturn(Optional.of(inventario));

        when(inventarioRepository.save(any(Inventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(movimientoRepository.save(
                any(MovimientoInventario.class)
        )).thenAnswer(invocation -> {
            MovimientoInventario movimiento =
                    invocation.getArgument(0);

            movimiento.setId(1L);
            movimiento.setFechaMovimiento(
                    LocalDateTime.of(2026, 7, 17, 10, 5)
            );

            return movimiento;
        });
    }
}