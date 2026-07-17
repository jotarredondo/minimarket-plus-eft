package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.entity.Categoria;
import com.duoc.minimarket.catalog_service.entity.Producto;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.CategoriaRepository;
import com.duoc.minimarket.catalog_service.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void configurarDatos() {
        categoria = Categoria.builder()
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
    }

    @Test
    void listarActivos_debeRetornarProductosActivos() {
        when(productoRepository.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(producto));

        List<ProductoResponse> resultado =
                productoService.listarActivos();

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals(
                        "BEB-001",
                        resultado.get(0).sku()
                ),
                () -> assertEquals(
                        "Bebidas",
                        resultado.get(0).categoriaNombre()
                )
        );

        verify(productoRepository)
                .findByActivoTrueOrderByNombreAsc();
    }

    @Test
    void listarPorCategoria_debeRetornarProductosDeCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository
                .findByCategoriaIdAndActivoTrueOrderByNombreAsc(1L))
                .thenReturn(List.of(producto));

        List<ProductoResponse> resultado =
                productoService.listarPorCategoria(1L);

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(
                        1L,
                        resultado.get(0).categoriaId()
                ),
                () -> assertEquals(
                        "Bebida Cola 1.5 L",
                        resultado.get(0).nombre()
                )
        );

        verify(categoriaRepository).findById(1L);

        verify(productoRepository)
                .findByCategoriaIdAndActivoTrueOrderByNombreAsc(1L);
    }

    @Test
    void crear_debeGuardarProductoNormalizado() {
        ProductoRequest request = new ProductoRequest(
                "  beb-002  ",
                "  Agua Mineral  ",
                "  Agua sin gas  ",
                new BigDecimal("1290.00"),
                1L
        );

        when(productoRepository.existsBySkuIgnoreCase("BEB-002"))
                .thenReturn(false);

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> {
                    Producto guardado = invocation.getArgument(0);
                    guardado.setId(2L);
                    return guardado;
                });

        ProductoResponse response =
                productoService.crear(request);

        ArgumentCaptor<Producto> captor =
                ArgumentCaptor.forClass(Producto.class);

        verify(productoRepository).save(captor.capture());

        Producto productoGuardado = captor.getValue();

        assertAll(
                () -> assertEquals(2L, response.id()),
                () -> assertEquals("BEB-002", response.sku()),
                () -> assertEquals(
                        "Agua Mineral",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Agua sin gas",
                        response.descripcion()
                ),
                () -> assertEquals(
                        new BigDecimal("1290.00"),
                        response.precio()
                ),
                () -> assertEquals(
                        categoria,
                        productoGuardado.getCategoria()
                ),
                () -> assertEquals(
                        "BEB-002",
                        productoGuardado.getSku()
                ),
                () -> assertEquals(
                        "Agua Mineral",
                        productoGuardado.getNombre()
                )
        );
    }

    @Test
    void crear_debeLanzarExcepcionCuandoSkuYaExiste() {
        ProductoRequest request = new ProductoRequest(
                "BEB-001",
                "Otro producto",
                null,
                new BigDecimal("1000.00"),
                1L
        );

        when(productoRepository.existsBySkuIgnoreCase("BEB-001"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> productoService.crear(request)
        );

        verify(categoriaRepository, never())
                .findById(any());

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarExcepcionCuandoCategoriaNoExiste() {
        ProductoRequest request = new ProductoRequest(
                "BEB-010",
                "Producto prueba",
                null,
                new BigDecimal("1000.00"),
                99L
        );

        when(productoRepository.existsBySkuIgnoreCase("BEB-010"))
                .thenReturn(false);

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.crear(request)
        );

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void actualizar_debeModificarProductoExistente() {
        ProductoRequest request = new ProductoRequest(
                "BEB-001",
                "Bebida Cola Actualizada",
                "Nueva descripción",
                new BigDecimal("2190.00"),
                1L
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(producto))
                .thenReturn(producto);

        ProductoResponse response =
                productoService.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        "Bebida Cola Actualizada",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Nueva descripción",
                        response.descripcion()
                ),
                () -> assertEquals(
                        new BigDecimal("2190.00"),
                        response.precio()
                ),
                () -> assertEquals(
                        "BEB-001",
                        response.sku()
                )
        );

        verify(productoRepository).save(producto);
    }

    @Test
    void actualizar_debeRechazarSkuDuplicadoCuandoCambia() {
        ProductoRequest request = new ProductoRequest(
                "BEB-999",
                "Producto actualizado",
                null,
                new BigDecimal("2000.00"),
                1L
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.existsBySkuIgnoreCase("BEB-999"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> productoService.actualizar(1L, request)
        );

        verify(categoriaRepository, never())
                .findById(any());

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void cambiarEstado_debeDesactivarProducto() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(productoRepository.save(producto))
                .thenReturn(producto);

        ProductoResponse response =
                productoService.cambiarEstado(1L, false);

        assertAll(
                () -> assertFalse(response.activo()),
                () -> assertFalse(producto.getActivo())
        );

        verify(productoRepository).save(producto);
    }

    @Test
    void obtenerPorId_debeRetornarProductoExistente() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        ProductoResponse response =
                productoService.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("BEB-001", response.sku()),
                () -> assertEquals(
                        "Bebida Cola 1.5 L",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Bebidas",
                        response.categoriaNombre()
                )
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoProductoNoExiste() {
        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.obtenerPorId(99L)
        );
    }
}
