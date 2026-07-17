package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.SucursalRequest;
import com.duoc.minimarket.catalog_service.dto.SucursalResponse;
import com.duoc.minimarket.catalog_service.entity.Sucursal;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalService(
            SucursalRepository sucursalRepository
    ) {
        this.sucursalRepository = sucursalRepository;
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> listarActivas() {
        return sucursalRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtenerPorId(Long id) {
        return convertirAResponse(
                buscarEntidadPorId(id)
        );
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request) {
        String codigoNormalizado = request.codigo()
                .trim()
                .toUpperCase();

        if (
                sucursalRepository
                        .existsByCodigoIgnoreCase(codigoNormalizado)
        ) {
            throw new RecursoDuplicadoException(
                    "Ya existe una sucursal con el código: "
                            + codigoNormalizado
            );
        }

        Sucursal sucursal = Sucursal.builder()
                .codigo(codigoNormalizado)
                .nombre(request.nombre().trim())
                .direccion(request.direccion().trim())
                .activo(true)
                .build();

        return convertirAResponse(
                sucursalRepository.save(sucursal)
        );
    }

    @Transactional
    public SucursalResponse actualizar(
            Long id,
            SucursalRequest request
    ) {
        Sucursal sucursal = buscarEntidadPorId(id);
        String codigoNormalizado = request.codigo()
                .trim()
                .toUpperCase();

        boolean codigoCambio =
                !sucursal.getCodigo()
                        .equalsIgnoreCase(codigoNormalizado);

        if (
                codigoCambio
                        && sucursalRepository
                        .existsByCodigoIgnoreCase(codigoNormalizado)
        ) {
            throw new RecursoDuplicadoException(
                    "Ya existe una sucursal con el código: "
                            + codigoNormalizado
            );
        }

        sucursal.setCodigo(codigoNormalizado);
        sucursal.setNombre(request.nombre().trim());
        sucursal.setDireccion(request.direccion().trim());

        return convertirAResponse(
                sucursalRepository.save(sucursal)
        );
    }

    @Transactional
    public SucursalResponse cambiarEstado(
            Long id,
            boolean activo
    ) {
        Sucursal sucursal = buscarEntidadPorId(id);
        sucursal.setActivo(activo);

        return convertirAResponse(
                sucursalRepository.save(sucursal)
        );
    }

    @Transactional(readOnly = true)
    public Sucursal buscarEntidadPorId(Long id) {
        return sucursalRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Sucursal",
                                id
                        )
                );
    }

    private SucursalResponse convertirAResponse(
            Sucursal sucursal
    ) {
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getCodigo(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getActivo()
        );
    }
}
