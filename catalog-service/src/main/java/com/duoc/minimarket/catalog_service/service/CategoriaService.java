package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.CategoriaRequest;
import com.duoc.minimarket.catalog_service.dto.CategoriaResponse;
import com.duoc.minimarket.catalog_service.entity.Categoria;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = buscarEntidadPorId(id);

        return convertirAResponse(categoria);
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombreNormalizado = request.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new RecursoDuplicadoException(
                    "Ya existe una categoría con el nombre: "
                            + nombreNormalizado
            );
        }

        Categoria categoria = Categoria.builder()
                .nombre(nombreNormalizado)
                .descripcion(normalizarDescripcion(request.descripcion()))
                .activo(true)
                .build();

        Categoria guardada = categoriaRepository.save(categoria);

        return convertirAResponse(guardada);
    }

    @Transactional
    public CategoriaResponse actualizar(
            Long id,
            CategoriaRequest request
    ) {
        Categoria categoria = buscarEntidadPorId(id);
        String nombreNormalizado = request.nombre().trim();

        boolean nombreCambio =
                !categoria.getNombre()
                        .equalsIgnoreCase(nombreNormalizado);

        if (
                nombreCambio
                        && categoriaRepository
                        .existsByNombreIgnoreCase(nombreNormalizado)
        ) {
            throw new RecursoDuplicadoException(
                    "Ya existe una categoría con el nombre: "
                            + nombreNormalizado
            );
        }

        categoria.setNombre(nombreNormalizado);
        categoria.setDescripcion(
                normalizarDescripcion(request.descripcion())
        );

        return convertirAResponse(
                categoriaRepository.save(categoria)
        );
    }

    @Transactional
    public CategoriaResponse cambiarEstado(
            Long id,
            boolean activo
    ) {
        Categoria categoria = buscarEntidadPorId(id);
        categoria.setActivo(activo);

        return convertirAResponse(
                categoriaRepository.save(categoria)
        );
    }

    @Transactional(readOnly = true)
    public Categoria buscarEntidadPorId(Long id) {
        return categoriaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Categoría",
                                id
                        )
                );
    }

    private CategoriaResponse convertirAResponse(
            Categoria categoria
    ) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo()
        );
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        return descripcion.trim();
    }
}