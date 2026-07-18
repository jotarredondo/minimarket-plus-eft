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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final CatalogClient catalogClient;

    public PromocionService(
            PromocionRepository promocionRepository,
            CatalogClient catalogClient
    ) {
        this.promocionRepository = promocionRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public PromocionResponse crear(
            PromocionRequest request,
            String authorizationHeader
    ) {
        validarRequest(request);

        CatalogProductoResponse producto =
                catalogClient.obtenerProducto(
                        request.productoId(),
                        authorizationHeader
                );

        if (!Boolean.TRUE.equals(producto.activo())) {
            throw new OperacionInvalidaException(
                    "No se puede crear una promoción para "
                            + "un producto inactivo"
            );
        }

        Promocion promocion = Promocion.builder()
                .nombre(request.nombre().trim())
                .descripcion(normalizarTexto(request.descripcion()))
                .productoId(request.productoId())
                .tipo(request.tipo())
                .valor(request.valor())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .activo(true)
                .build();

        promocion.antesDeCrear();

        return convertirResponse(
                promocionRepository.save(promocion)
        );
    }

    @Transactional(readOnly = true)
    public List<PromocionResponse> listarActivas() {
        LocalDateTime ahora = LocalDateTime.now();

        return promocionRepository
                .findByActivoTrueOrderByFechaInicioDesc()
                .stream()
                .filter(promocion ->
                        promocion.esVigente(ahora)
                )
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional
    public PromocionResponse cambiarEstado(
            Long promocionId,
            boolean activo
    ) {
        Promocion promocion =
                promocionRepository
                        .findById(promocionId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Promoción no encontrada: "
                                                + promocionId
                                )
                        );

        promocion.setActivo(activo);

        return convertirResponse(
                promocionRepository.save(promocion)
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularMejorDescuento(
            Long productoId,
            BigDecimal precioUnitario,
            Integer cantidad
    ) {
        LocalDateTime ahora = LocalDateTime.now();

        return promocionRepository
                .findByProductoIdAndActivoTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        productoId,
                        ahora,
                        ahora
                )
                .stream()
                .map(promocion ->
                        promocion.calcularDescuento(
                                precioUnitario,
                                cantidad,
                                ahora
                        )
                )
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private void validarRequest(
            PromocionRequest request
    ) {
        if (!request.fechaFin()
                .isAfter(request.fechaInicio())) {

            throw new OperacionInvalidaException(
                    "La fecha de término debe ser posterior "
                            + "a la fecha de inicio"
            );
        }

        if (request.tipo() == TipoPromocion.PORCENTAJE
                && request.valor().compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            throw new OperacionInvalidaException(
                    "El porcentaje no puede superar el 100%"
            );
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }

    private PromocionResponse convertirResponse(
            Promocion promocion
    ) {
        return new PromocionResponse(
                promocion.getId(),
                promocion.getNombre(),
                promocion.getDescripcion(),
                promocion.getProductoId(),
                promocion.getTipo(),
                promocion.getValor(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getActivo()
        );
    }
}
