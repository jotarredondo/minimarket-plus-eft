package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.entity.Categoria;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.CategoriaRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void configurarDatos() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Bebidas")
                .descripcion("Bebidas y refrescos")
                .activo(true)
                .build();
    }

    @Test
    void listarActivas_debeRetornarCategoriasActivas() {
        when(categoriaRepository.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(categoria));

        List<CategoriaResponse> resultado =
                categoriaService.listarActivas();

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals(
                        "Bebidas",
                        resultado.get(0).nombre()
                ),
                () -> assertEquals(
                        "Bebidas y refrescos",
                        resultado.get(0).descripcion()
                )
        );

        verify(categoriaRepository)
                .findByActivoTrueOrderByNombreAsc();
    }

    @Test
    void obtenerPorId_debeRetornarCategoriaExistente() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaResponse response =
                categoriaService.obtenerPorId(1L);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("Bebidas", response.nombre()),
                () -> assertEquals(
                        "Bebidas y refrescos",
                        response.descripcion()
                ),
                () -> assertEquals(true, response.activo())
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.obtenerPorId(99L)
        );
    }

    @Test
    void crear_debeGuardarCategoriaNormalizada() {
        CategoriaRequest request = new CategoriaRequest(
                "  Alimentos  ",
                "  Productos alimenticios  "
        );

        when(categoriaRepository
                .existsByNombreIgnoreCase("Alimentos"))
                .thenReturn(false);

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> {
                    Categoria guardada = invocation.getArgument(0);
                    guardada.setId(2L);
                    return guardada;
                });

        CategoriaResponse response =
                categoriaService.crear(request);

        ArgumentCaptor<Categoria> captor =
                ArgumentCaptor.forClass(Categoria.class);

        verify(categoriaRepository).save(captor.capture());

        Categoria guardada = captor.getValue();

        assertAll(
                () -> assertEquals(2L, response.id()),
                () -> assertEquals("Alimentos", response.nombre()),
                () -> assertEquals(
                        "Productos alimenticios",
                        response.descripcion()
                ),
                () -> assertEquals("Alimentos", guardada.getNombre()),
                () -> assertEquals(
                        "Productos alimenticios",
                        guardada.getDescripcion()
                ),
                () -> assertEquals(true, guardada.getActivo())
        );
    }

    @Test
    void crear_debeConvertirDescripcionVaciaEnNull() {
        CategoriaRequest request = new CategoriaRequest(
                "Limpieza",
                "   "
        );

        when(categoriaRepository
                .existsByNombreIgnoreCase("Limpieza"))
                .thenReturn(false);

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> {
                    Categoria guardada = invocation.getArgument(0);
                    guardada.setId(3L);
                    return guardada;
                });

        CategoriaResponse response =
                categoriaService.crear(request);

        assertNull(response.descripcion());
    }

    @Test
    void crear_debeLanzarExcepcionCuandoNombreYaExiste() {
        CategoriaRequest request = new CategoriaRequest(
                "Bebidas",
                "Categoría duplicada"
        );

        when(categoriaRepository
                .existsByNombreIgnoreCase("Bebidas"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> categoriaService.crear(request)
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void actualizar_debeModificarCategoriaExistente() {
        CategoriaRequest request = new CategoriaRequest(
                "Bebidas Actualizadas",
                "Nueva descripción"
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository
                .existsByNombreIgnoreCase("Bebidas Actualizadas"))
                .thenReturn(false);

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        CategoriaResponse response =
                categoriaService.actualizar(1L, request);

        assertAll(
                () -> assertEquals(
                        "Bebidas Actualizadas",
                        response.nombre()
                ),
                () -> assertEquals(
                        "Nueva descripción",
                        response.descripcion()
                )
        );

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void actualizar_debePermitirMantenerElMismoNombre() {
        CategoriaRequest request = new CategoriaRequest(
                "Bebidas",
                "Descripción modificada"
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        CategoriaResponse response =
                categoriaService.actualizar(1L, request);

        assertEquals(
                "Descripción modificada",
                response.descripcion()
        );

        verify(categoriaRepository, never())
                .existsByNombreIgnoreCase(any());

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void actualizar_debeRechazarNombreDuplicadoCuandoCambia() {
        CategoriaRequest request = new CategoriaRequest(
                "Alimentos",
                "Nueva categoría"
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository
                .existsByNombreIgnoreCase("Alimentos"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> categoriaService.actualizar(1L, request)
        );

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void cambiarEstado_debeDesactivarCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        CategoriaResponse response =
                categoriaService.cambiarEstado(1L, false);

        assertAll(
                () -> assertFalse(response.activo()),
                () -> assertFalse(categoria.getActivo())
        );

        verify(categoriaRepository).save(categoria);
    }
}
