package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.PromocionRequest;
import com.duoc.minimarket.sales_service.dto.PromocionResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Promocion;
import com.duoc.minimarket.sales_service.entity.TipoPromocion;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.repository.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceCoverageTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private CatalogClient catalogClient;

    private PromocionService promocionService;

    @BeforeEach
    void setUp() {
        promocionService = new PromocionService(
                promocionRepository,
                catalogClient
        );
    }

    @Test
    void debeListarSoloPromocionesActivasYVigentes() {
        LocalDateTime ahora = LocalDateTime.now();

        Promocion vigente = crearPromocion(
                1L,
                TipoPromocion.PORCENTAJE,
                new BigDecimal("10"),
                ahora.minusDays(1),
                ahora.plusDays(10),
                true
        );

        Promocion expirada = crearPromocion(
                2L,
                TipoPromocion.PORCENTAJE,
                new BigDecimal("20"),
                ahora.minusDays(10),
                ahora.minusDays(1),
                true
        );

        when(
                promocionRepository
                        .findByActivoTrueOrderByFechaInicioDesc()
        ).thenReturn(List.of(vigente, expirada));

        List<PromocionResponse> responses =
                promocionService.listarActivas();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
    }

    @Test
    void debeDesactivarPromocion() {
        Promocion promocion = crearPromocion(
                1L,
                TipoPromocion.PORCENTAJE,
                new BigDecimal("10"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                true
        );

        when(promocionRepository.findById(1L))
                .thenReturn(Optional.of(promocion));

        when(promocionRepository.save(any(Promocion.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PromocionResponse response =
                promocionService.cambiarEstado(
                        1L,
                        false
                );

        assertFalse(response.activo());
        assertFalse(promocion.getActivo());
    }

    @Test
    void debeActivarPromocion() {
        Promocion promocion = crearPromocion(
                1L,
                TipoPromocion.PORCENTAJE,
                new BigDecimal("10"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                false
        );

        when(promocionRepository.findById(1L))
                .thenReturn(Optional.of(promocion));

        when(promocionRepository.save(any(Promocion.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PromocionResponse response =
                promocionService.cambiarEstado(
                        1L,
                        true
                );

        assertTrue(response.activo());
    }

    @Test
    void debeInformarPromocionNoEncontrada() {
        when(promocionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> promocionService
                        .cambiarEstado(99L, false)
        );
    }

    @Test
    void debeRechazarPorcentajeSuperiorACien() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(10);

        PromocionRequest request =
                new PromocionRequest(
                        "Promoción inválida",
                        null,
                        1L,
                        TipoPromocion.PORCENTAJE,
                        new BigDecimal("101"),
                        inicio,
                        fin
                );

        assertThrows(
                OperacionInvalidaException.class,
                () -> promocionService.crear(
                        request,
                        "Bearer token"
                )
        );

        verify(catalogClient, never())
                .obtenerProducto(any(), any());
    }

    @Test
    void debeRechazarProductoInactivo() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(10);

        PromocionRequest request =
                new PromocionRequest(
                        "Promoción inválida",
                        null,
                        1L,
                        TipoPromocion.PORCENTAJE,
                        new BigDecimal("10"),
                        inicio,
                        fin
                );

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token"
                )
        ).thenReturn(
                new CatalogProductoResponse(
                        1L,
                        "BEB-001",
                        "Bebida Cola",
                        null,
                        new BigDecimal("1990.00"),
                        false,
                        1L,
                        "Bebidas"
                )
        );

        assertThrows(
                OperacionInvalidaException.class,
                () -> promocionService.crear(
                        request,
                        "Bearer token"
                )
        );

        verify(promocionRepository, never())
                .save(any(Promocion.class));
    }

    @Test
    void debeCalcularDescuentoDeMontoFijo() {
        LocalDateTime ahora = LocalDateTime.now();

        Promocion promocion = crearPromocion(
                1L,
                TipoPromocion.MONTO_FIJO,
                new BigDecimal("300.00"),
                ahora.minusDays(1),
                ahora.plusDays(10),
                true
        );

        when(
                promocionRepository
                        .findByProductoIdAndActivoTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                any(Long.class),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(List.of(promocion));

        BigDecimal descuento =
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1000.00"),
                        2
                );

        assertTrue(
                descuento.compareTo(BigDecimal.ZERO) > 0
        );

        assertTrue(
                descuento.compareTo(
                        new BigDecimal("2000.00")
                ) <= 0
        );
    }

    @Test
    void debeRetornarCeroCuandoNoHayPromociones() {
        when(
                promocionRepository
                        .findByProductoIdAndActivoTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                any(Long.class),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(List.of());

        BigDecimal descuento =
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1000.00"),
                        2
                );

        assertEquals(
                0,
                descuento.compareTo(BigDecimal.ZERO)
        );
    }

    private Promocion crearPromocion(
            Long id,
            TipoPromocion tipo,
            BigDecimal valor,
            LocalDateTime inicio,
            LocalDateTime fin,
            boolean activo
    ) {
        return Promocion.builder()
                .id(id)
                .nombre("Promoción " + id)
                .descripcion("Promoción de prueba")
                .productoId(1L)
                .tipo(tipo)
                .valor(valor)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .activo(activo)
                .build();
    }
}