package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.dto.*;
import com.duoc.minimarket.catalog_service.entity.EstadoOrdenReposicion;
import com.duoc.minimarket.catalog_service.entity.Inventario;
import com.duoc.minimarket.catalog_service.entity.MovimientoInventario;
import com.duoc.minimarket.catalog_service.entity.OrdenReposicion;
import com.duoc.minimarket.catalog_service.entity.Producto;
import com.duoc.minimarket.catalog_service.entity.Sucursal;
import com.duoc.minimarket.catalog_service.entity.TipoMovimientoInventario;
import com.duoc.minimarket.catalog_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.catalog_service.exception.RecursoDuplicadoException;
import com.duoc.minimarket.catalog_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.catalog_service.exception.StockInsuficienteException;
import com.duoc.minimarket.catalog_service.repository.InventarioRepository;
import com.duoc.minimarket.catalog_service.repository.MovimientoInventarioRepository;
import com.duoc.minimarket.catalog_service.repository.OrdenReposicionRepository;
import com.duoc.minimarket.catalog_service.repository.ProductoRepository;
import com.duoc.minimarket.catalog_service.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final OrdenReposicionRepository ordenReposicionRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public InventarioService(
            InventarioRepository inventarioRepository,
            MovimientoInventarioRepository movimientoRepository,
            OrdenReposicionRepository ordenReposicionRepository,
            ProductoRepository productoRepository,
            SucursalRepository sucursalRepository
    ) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.ordenReposicionRepository = ordenReposicionRepository;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    /**
     * Crea un registro de inventario para un producto y una sucursal.
     * La combinación producto-sucursal debe ser única.
     */
    @Transactional
    public InventarioResponse crear(
            CrearInventarioRequest request,
            String usuarioEmail
    ) {
        if (inventarioRepository.existsByProductoIdAndSucursalId(
                request.productoId(),
                request.sucursalId()
        )) {
            throw new RecursoDuplicadoException(
                    "Ya existe inventario para el producto "
                            + request.productoId()
                            + " en la sucursal "
                            + request.sucursalId()
            );
        }

        Producto producto = productoRepository
                .findById(request.productoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto",
                                request.productoId()
                        )
                );

        Sucursal sucursal = sucursalRepository
                .findById(request.sucursalId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Sucursal",
                                request.sucursalId()
                        )
                );

        Inventario inventario = Inventario.builder()
                .producto(producto)
                .sucursal(sucursal)
                .stockActual(request.stockInicial())
                .stockMinimo(request.stockMinimo())
                .build();

        Inventario guardado = inventarioRepository.save(inventario);

        registrarHistorial(
                guardado,
                TipoMovimientoInventario.AJUSTE,
                request.stockInicial(),
                0,
                request.stockInicial(),
                "Creación inicial del inventario",
                usuarioEmail
        );

        generarOrdenReposicionSiCorresponde(guardado);

        return convertirInventarioAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorId(Long id) {
        return convertirInventarioAResponse(
                buscarInventarioPorId(id)
        );
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> listarPorProducto(
            Long productoId
    ) {
        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException(
                    "Producto",
                    productoId
            );
        }

        return inventarioRepository
                .findByProductoIdOrderBySucursalNombreAsc(productoId)
                .stream()
                .map(this::convertirInventarioAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> listarPorSucursal(
            Long sucursalId
    ) {
        if (!sucursalRepository.existsById(sucursalId)) {
            throw new RecursoNoEncontradoException(
                    "Sucursal",
                    sucursalId
            );
        }

        return inventarioRepository
                .findBySucursalIdOrderByProductoNombreAsc(sucursalId)
                .stream()
                .map(this::convertirInventarioAResponse)
                .toList();
    }

    /**
     * Registra una entrada, salida o ajuste de inventario.
     */
    @Transactional
    public MovimientoInventarioResponse registrarMovimiento(
            Long inventarioId,
            MovimientoInventarioRequest request,
            String usuarioEmail
    ) {
        Inventario inventario = buscarInventarioPorId(inventarioId);

        int stockAnterior = inventario.getStockActual();
        int stockPosterior = calcularStockPosterior(
                stockAnterior,
                request
        );

        inventario.setStockActual(stockPosterior);
        Inventario actualizado =
                inventarioRepository.save(inventario);

        MovimientoInventario movimiento = registrarHistorial(
                actualizado,
                request.tipo(),
                request.cantidad(),
                stockAnterior,
                stockPosterior,
                request.motivo().trim(),
                usuarioEmail
        );

        generarOrdenReposicionSiCorresponde(actualizado);

        return convertirMovimientoAResponse(movimiento);
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> listarMovimientos(
            Long inventarioId
    ) {
        buscarInventarioPorId(inventarioId);

        return movimientoRepository
                .findByInventarioIdOrderByFechaMovimientoDesc(
                        inventarioId
                )
                .stream()
                .map(this::convertirMovimientoAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenReposicionResponse> listarOrdenesPorEstado(
            EstadoOrdenReposicion estado
    ) {
        return ordenReposicionRepository
                .findByEstadoOrderByFechaGeneracionDesc(estado)
                .stream()
                .map(this::convertirOrdenAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenReposicionResponse> listarOrdenesPorInventario(
            Long inventarioId
    ) {
        buscarInventarioPorId(inventarioId);

        return ordenReposicionRepository
                .findByInventarioIdOrderByFechaGeneracionDesc(
                        inventarioId
                )
                .stream()
                .map(this::convertirOrdenAResponse)
                .toList();
    }

    @Transactional
    public OrdenReposicionResponse actualizarEstadoOrden(
            Long ordenId,
            ActualizarOrdenReposicionRequest request
    ) {
        OrdenReposicion orden = ordenReposicionRepository
                .findById(ordenId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden de reposición",
                                ordenId
                        )
                );

        orden.setEstado(request.estado());

        return convertirOrdenAResponse(
                ordenReposicionRepository.save(orden)
        );
    }

    private int calcularStockPosterior(
            int stockAnterior,
            MovimientoInventarioRequest request
    ) {
        int cantidad = request.cantidad();

        return switch (request.tipo()) {
            case ENTRADA -> {
                validarCantidadMayorQueCero(
                        cantidad,
                        "La entrada debe ser mayor que cero"
                );

                yield stockAnterior + cantidad;
            }

            case SALIDA -> {
                validarCantidadMayorQueCero(
                        cantidad,
                        "La salida debe ser mayor que cero"
                );

                if (cantidad > stockAnterior) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente. Disponible: "
                                    + stockAnterior
                                    + ", solicitado: "
                                    + cantidad
                    );
                }

                yield stockAnterior - cantidad;
            }

            case AJUSTE -> cantidad;
        };
    }

    private void validarCantidadMayorQueCero(
            int cantidad,
            String mensaje
    ) {
        if (cantidad <= 0) {
            throw new OperacionInvalidaException(mensaje);
        }
    }

    private MovimientoInventario registrarHistorial(
            Inventario inventario,
            TipoMovimientoInventario tipo,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockPosterior,
            String motivo,
            String usuarioEmail
    ) {
        MovimientoInventario movimiento =
                MovimientoInventario.builder()
                        .inventario(inventario)
                        .tipo(tipo)
                        .cantidad(cantidad)
                        .stockAnterior(stockAnterior)
                        .stockPosterior(stockPosterior)
                        .motivo(motivo)
                        .usuarioEmail(usuarioEmail)
                        .build();

        return movimientoRepository.save(movimiento);
    }

    /**
     * Genera una orden solamente cuando el stock alcanza o baja
     * del mínimo y no existe otra orden GENERADA para ese inventario.
     */
    private void generarOrdenReposicionSiCorresponde(
            Inventario inventario
    ) {
        boolean requiereReposicion =
                inventario.getStockActual()
                        <= inventario.getStockMinimo();

        if (!requiereReposicion) {
            return;
        }

        boolean yaExisteOrden =
                ordenReposicionRepository
                        .existsByInventarioIdAndEstado(
                                inventario.getId(),
                                EstadoOrdenReposicion.GENERADA
                        );

        if (yaExisteOrden) {
            return;
        }

        int cantidadSugerida = calcularCantidadSugerida(
                inventario
        );

        OrdenReposicion orden = OrdenReposicion.builder()
                .inventario(inventario)
                .cantidadSugerida(cantidadSugerida)
                .estado(EstadoOrdenReposicion.GENERADA)
                .motivo(
                        "Stock actual igual o inferior al stock mínimo"
                )
                .build();

        ordenReposicionRepository.save(orden);
    }

    /**
     * Sugiere recuperar el inventario hasta dos veces el stock mínimo.
     */
    private int calcularCantidadSugerida(
            Inventario inventario
    ) {
        int stockObjetivo =
                inventario.getStockMinimo() * 2;

        int diferencia =
                stockObjetivo - inventario.getStockActual();

        return Math.max(diferencia, 1);
    }

    private Inventario buscarInventarioPorId(Long id) {
        return inventarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inventario",
                                id
                        )
                );
    }

    private InventarioResponse convertirInventarioAResponse(
            Inventario inventario
    ) {
        return new InventarioResponse(
                inventario.getId(),
                inventario.getProducto().getId(),
                inventario.getProducto().getSku(),
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getId(),
                inventario.getSucursal().getCodigo(),
                inventario.getSucursal().getNombre(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockActual()
                        <= inventario.getStockMinimo(),
                inventario.getFechaActualizacion()
        );
    }

    private MovimientoInventarioResponse convertirMovimientoAResponse(
            MovimientoInventario movimiento
    ) {
        return new MovimientoInventarioResponse(
                movimiento.getId(),
                movimiento.getInventario().getId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockPosterior(),
                movimiento.getMotivo(),
                movimiento.getUsuarioEmail(),
                movimiento.getFechaMovimiento()
        );
    }

    private OrdenReposicionResponse convertirOrdenAResponse(
            OrdenReposicion orden
    ) {
        Inventario inventario = orden.getInventario();

        return new OrdenReposicionResponse(
                orden.getId(),
                inventario.getId(),
                inventario.getProducto().getId(),
                inventario.getProducto().getNombre(),
                inventario.getSucursal().getId(),
                inventario.getSucursal().getNombre(),
                orden.getCantidadSugerida(),
                orden.getEstado(),
                orden.getMotivo(),
                orden.getFechaGeneracion()
        );
    }

    @Transactional
    public MovimientoInventarioResponse registrarSalidaVenta(
            Long inventarioId,
            SalidaVentaRequest request,
            String usuarioEmail
    ) {
        MovimientoInventarioRequest movimientoRequest =
                new MovimientoInventarioRequest(
                        TipoMovimientoInventario.SALIDA,
                        request.cantidad(),
                        request.motivo().trim()
                );

        return registrarMovimiento(
                inventarioId,
                movimientoRequest,
                usuarioEmail
        );
    }
}
