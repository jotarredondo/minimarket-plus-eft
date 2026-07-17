package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.ProductoRequest;
import com.duoc.minimarket.catalog_service.dto.ProductoResponse;
import com.duoc.minimarket.catalog_service.entity.Categoria;
import com.duoc.minimarket.catalog_service.entity.Producto;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.CategoriaRepository;
import com.duoc.minimarket.catalog_service.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarActivos() {
        return productoRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(
            Long categoriaId
    ) {
        buscarCategoria(categoriaId);

        return productoRepository
                .findByCategoriaIdAndActivoTrueOrderByNombreAsc(
                        categoriaId
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return convertirAResponse(
                buscarEntidadPorId(id)
        );
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String skuNormalizado = request.sku()
                .trim()
                .toUpperCase();

        if (productoRepository.existsBySkuIgnoreCase(skuNormalizado)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un producto con el SKU: "
                            + skuNormalizado
            );
        }

        Categoria categoria =
                buscarCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .sku(skuNormalizado)
                .nombre(request.nombre().trim())
                .descripcion(
                        normalizarDescripcion(request.descripcion())
                )
                .precio(request.precio())
                .activo(true)
                .categoria(categoria)
                .build();

        return convertirAResponse(
                productoRepository.save(producto)
        );
    }

    @Transactional
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request
    ) {
        Producto producto = buscarEntidadPorId(id);
        String skuNormalizado = request.sku()
                .trim()
                .toUpperCase();

        boolean skuCambio =
                !producto.getSku()
                        .equalsIgnoreCase(skuNormalizado);

        if (
                skuCambio
                        && productoRepository
                        .existsBySkuIgnoreCase(skuNormalizado)
        ) {
            throw new RecursoDuplicadoException(
                    "Ya existe un producto con el SKU: "
                            + skuNormalizado
            );
        }

        Categoria categoria =
                buscarCategoria(request.categoriaId());

        producto.setSku(skuNormalizado);
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(
                normalizarDescripcion(request.descripcion())
        );
        producto.setPrecio(request.precio());
        producto.setCategoria(categoria);

        return convertirAResponse(
                productoRepository.save(producto)
        );
    }

    @Transactional
    public ProductoResponse cambiarEstado(
            Long id,
            boolean activo
    ) {
        Producto producto = buscarEntidadPorId(id);
        producto.setActivo(activo);

        return convertirAResponse(
                productoRepository.save(producto)
        );
    }

    @Transactional(readOnly = true)
    public Producto buscarEntidadPorId(Long id) {
        return productoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto",
                                id
                        )
                );
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Categoría",
                                categoriaId
                        )
                );
    }

    private ProductoResponse convertirAResponse(
            Producto producto
    ) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getActivo(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        return descripcion.trim();
    }
}
