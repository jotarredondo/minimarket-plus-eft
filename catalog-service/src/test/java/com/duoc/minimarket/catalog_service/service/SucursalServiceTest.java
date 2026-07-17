package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.entity.Sucursal;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalService sucursalService;

    private Sucursal sucursal;

    @BeforeEach
    void configurarDatos() {
        sucursal = Sucursal.builder()
                .id(1L)
                .codigo("SUC-001")
                .nombre("Sucursal Centro")
                .direccion("Avenida Principal 100")
                .activo(true)
                .build();
    }

    @Test
    void listarActivas_debeRetornarSucursalesActivas() {
        when(sucursalRepository.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(sucursal));

        List<SucursalResponse> resultado =
                sucursalService.listarActivas();

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals(
                        "SUC-001",
                        resultado.get(0).codigo()
                ),
                () -> assertEquals(
                        "Sucursal Centro",
                        resultado.get(0).nombre()
                ),
                () -> assertEquals(
                        "Avenida Principal 100",
                        resultado.get(0).direccion()
                )
        );

        verify(sucursalRepository)
                .findByActivoTrueOrderByNombreAsc();
    }

    @Test
    void obtenerPorId_debeRetornarSucursalExistente() {
        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        SucursalResponse response =
                sucursalService.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("SUC-001", response.codigo()),
                () -> assertEquals(
                        "Sucursal Centro",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Avenida Principal 100",
                        response.direccion()
                ),
                () -> assertEquals(true, response.activo())
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(sucursalRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> sucursalService.obtenerPorId(99L)
        );
    }

    @Test
    void crear_debeGuardarSucursalNormalizada() {
        SucursalRequest request = new SucursalRequest(
                "  suc-002  ",
                "  Sucursal Norte  ",
                "  Calle Norte 500  "
        );

        when(sucursalRepository
                .existsByCodigoIgnoreCase("SUC-002"))
                .thenReturn(false);

        when(sucursalRepository.save(any(Sucursal.class)))
                .thenAnswer(invocation -> {
                    Sucursal guardada = invocation.getArgument(0);
                    guardada.setId(2L);
                    return guardada;
                });

        SucursalResponse response =
                sucursalService.crear(request);

        ArgumentCaptor<Sucursal> captor =
                ArgumentCaptor.forClass(Sucursal.class);

        verify(sucursalRepository).save(captor.capture());

        Sucursal guardada = captor.getValue();

        assertAll(
                () -> assertEquals(2L, response.id()),
                () -> assertEquals("SUC-002", response.codigo()),
                () -> assertEquals(
                        "Sucursal Norte",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Calle Norte 500",
                        response.direccion()
                ),
                () -> assertEquals("SUC-002", guardada.getCodigo()),
                () -> assertEquals(
                        "Sucursal Norte",
                        guardada.getNombre()
                ),
                () -> assertEquals(
                        "Calle Norte 500",
                        guardada.getDireccion()
                ),
                () -> assertEquals(true, guardada.getActivo())
        );
    }

    @Test
    void crear_debeLanzarExcepcionCuandoCodigoYaExiste() {
        SucursalRequest request = new SucursalRequest(
                "SUC-001",
                "Sucursal duplicada",
                "Dirección duplicada"
        );

        when(sucursalRepository
                .existsByCodigoIgnoreCase("SUC-001"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sucursalService.crear(request)
        );

        verify(sucursalRepository, never())
                .save(any(Sucursal.class));
    }

    @Test
    void actualizar_debeModificarSucursalExistente() {
        SucursalRequest request = new SucursalRequest(
                "SUC-001",
                "Sucursal Centro Actualizada",
                "Nueva Avenida 200"
        );

        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository.save(sucursal))
                .thenReturn(sucursal);

        SucursalResponse response =
                sucursalService.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        "SUC-001",
                        response.codigo()
                ),
                () -> assertEquals(
                        "Sucursal Centro Actualizada",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Nueva Avenida 200",
                        response.direccion()
                )
        );

        verify(sucursalRepository, never())
                .existsByCodigoIgnoreCase(any());

        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void actualizar_debeAceptarNuevoCodigoDisponible() {
        SucursalRequest request = new SucursalRequest(
                "SUC-010",
                "Sucursal Centro",
                "Avenida Principal 100"
        );

        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository
                .existsByCodigoIgnoreCase("SUC-010"))
                .thenReturn(false);

        when(sucursalRepository.save(sucursal))
                .thenReturn(sucursal);

        SucursalResponse response =
                sucursalService.actualizar(1L, request);

        assertEquals("SUC-010", response.codigo());

        verify(sucursalRepository)
                .existsByCodigoIgnoreCase("SUC-010");

        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void actualizar_debeRechazarCodigoDuplicadoCuandoCambia() {
        SucursalRequest request = new SucursalRequest(
                "SUC-999",
                "Sucursal modificada",
                "Dirección modificada"
        );

        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository
                .existsByCodigoIgnoreCase("SUC-999"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> sucursalService.actualizar(1L, request)
        );

        verify(sucursalRepository, never())
                .save(any(Sucursal.class));
    }

    @Test
    void cambiarEstado_debeDesactivarSucursal() {
        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository.save(sucursal))
                .thenReturn(sucursal);

        SucursalResponse response =
                sucursalService.cambiarEstado(1L, false);

        assertAll(
                () -> assertFalse(response.activo()),
                () -> assertFalse(sucursal.getActivo())
        );

        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void buscarEntidadPorId_debeRetornarSucursalExistente() {
        when(sucursalRepository.findById(1L))
                .thenReturn(Optional.of(sucursal));

        Sucursal resultado =
                sucursalService.buscarEntidadPorId(1L);

        assertEquals(sucursal, resultado);
        verify(sucursalRepository).findById(1L);
    }
}
