package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.PromocionRequest;
import com.duoc.minimarket.sales_service.dto.PromocionResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Promocion;
import com.duoc.minimarket.sales_service.entity.TipoPromocion;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.repository.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

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
    void debeCrearPromocionValida() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(30);

        PromocionRequest request =
                new PromocionRequest(
                        "Descuento bebidas",
                        "Promoción de prueba",
                        1L,
                        TipoPromocion.PORCENTAJE,
                        new BigDecimal("10"),
                        inicio,
                        fin
                );

        when(
                catalogClient.obtenerProducto(
                        1L,
                        "Bearer token-admin"
                )
        ).thenReturn(crearProducto());

        when(promocionRepository.save(any(Promocion.class)))
                .thenAnswer(invocation -> {
                    Promocion promocion =
                            invocation.getArgument(0);

                    promocion.setId(1L);
                    return promocion;
                });

        PromocionResponse response =
                promocionService.crear(
                        request,
                        "Bearer token-admin"
                );

        assertEquals(1L, response.id());
        assertEquals(
                TipoPromocion.PORCENTAJE,
                response.tipo()
        );
        assertEquals(true, response.activo());
    }

    @Test
    void debeRechazarFechaFinAnterior() {
        LocalDateTime inicio =
                LocalDateTime.now().plusDays(10);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(1);

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

        assertThrows(
                OperacionInvalidaException.class,
                () -> promocionService.crear(
                        request,
                        "Bearer token-admin"
                )
        );
    }

    @Test
    void debeCalcularMejorDescuento() {
        LocalDateTime inicio =
                LocalDateTime.now().minusDays(1);

        LocalDateTime fin =
                LocalDateTime.now().plusDays(30);

        Promocion promocion10 = Promocion.builder()
                .id(1L)
                .nombre("Descuento 10")
                .productoId(1L)
                .tipo(TipoPromocion.PORCENTAJE)
                .valor(new BigDecimal("10"))
                .fechaInicio(inicio)
                .fechaFin(fin)
                .activo(true)
                .build();

        Promocion promocion20 = Promocion.builder()
                .id(2L)
                .nombre("Descuento 20")
                .productoId(1L)
                .tipo(TipoPromocion.PORCENTAJE)
                .valor(new BigDecimal("20"))
                .fechaInicio(inicio)
                .fechaFin(fin)
                .activo(true)
                .build();

        when(
                promocionRepository
                        .findByProductoIdAndActivoTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                                any(Long.class),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                List.of(promocion10, promocion20)
        );

        BigDecimal descuento =
                promocionService.calcularMejorDescuento(
                        1L,
                        new BigDecimal("1000.00"),
                        2
                );

        assertEquals(
                0,
                descuento.compareTo(
                        new BigDecimal("400.00")
                )
        );
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
}
