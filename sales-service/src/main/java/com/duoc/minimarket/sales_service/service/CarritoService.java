package com.duoc.minimarket.sales_service.service;

import com.duoc.minimarket.sales_service.client.CatalogClient;
import com.duoc.minimarket.sales_service.dto.ActualizarCantidadItemRequest;
import com.duoc.minimarket.sales_service.dto.AgregarItemCarritoRequest;
import com.duoc.minimarket.sales_service.dto.CarritoResponse;
import com.duoc.minimarket.sales_service.dto.CrearCarritoRequest;
import com.duoc.minimarket.sales_service.dto.ItemCarritoResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.entity.Carrito;
import com.duoc.minimarket.sales_service.entity.EstadoCarrito;
import com.duoc.minimarket.sales_service.entity.ItemCarrito;
import com.duoc.minimarket.sales_service.exception.OperacionInvalidaException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
import com.duoc.minimarket.sales_service.repository.CarritoRepository;
import com.duoc.minimarket.sales_service.repository.ItemCarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final CatalogClient catalogClient;
    private final PromocionService promocionService;

    public CarritoService(
            CarritoRepository carritoRepository,
            ItemCarritoRepository itemCarritoRepository,
            CatalogClient catalogClient,
            PromocionService promocionService
    ) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.catalogClient = catalogClient;
        this.promocionService = promocionService;
    }

    /**
     * Crea un carrito activo para el cliente.
     * Si ya existe uno para la misma sucursal, lo recupera.
     */
    @Transactional
    public CarritoResponse crearORecuperar(
            String clienteEmail,
            CrearCarritoRequest request
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        Carrito carritoExistente =
                carritoRepository
                        .findByClienteEmailAndEstado(
                                emailNormalizado,
                                EstadoCarrito.ACTIVO
                        )
                        .orElse(null);

        if (carritoExistente != null) {
            if (!carritoExistente
                    .getSucursalId()
                    .equals(request.sucursalId())) {

                throw new OperacionInvalidaException(
                        "El cliente ya posee un carrito activo "
                                + "en otra sucursal"
                );
            }

            return convertirResponse(carritoExistente);
        }

        Carrito nuevoCarrito = Carrito.builder()
                .clienteEmail(emailNormalizado)
                .sucursalId(request.sucursalId())
                .estado(EstadoCarrito.ACTIVO)
                .subtotal(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        Carrito carritoGuardado =
                carritoRepository.save(nuevoCarrito);

        return convertirResponse(carritoGuardado);
    }

    /**
     * Obtiene el carrito activo del cliente autenticado.
     */
    @Transactional(readOnly = true)
    public CarritoResponse obtenerActivo(
            String clienteEmail
    ) {
        Carrito carrito =
                obtenerCarritoActivo(
                        normalizarEmail(clienteEmail)
                );

        return convertirResponse(carrito);
    }

    /**
     * Agrega un producto al carrito.
     *
     * Valida:
     * - Que exista el inventario.
     * - Que pertenezca a la sucursal del carrito.
     * - Que exista stock.
     * - Que el producto esté activo.
     * - Que el precio sea válido.
     * - Que exista una promoción aplicable.
     */
    @Transactional
    public CarritoResponse agregarItem(
            String clienteEmail,
            AgregarItemCarritoRequest request,
            String authorizationHeader
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        validarAuthorizationHeader(
                authorizationHeader
        );

        Carrito carrito =
                obtenerCarritoActivo(emailNormalizado);

        CatalogInventarioResponse inventario =
                catalogClient.obtenerInventario(
                        request.inventarioId(),
                        authorizationHeader
                );

        validarInventarioSucursal(
                carrito,
                inventario
        );

        CatalogProductoResponse producto =
                catalogClient.obtenerProducto(
                        inventario.productoId(),
                        authorizationHeader
                );

        validarProducto(
                inventario,
                producto
        );

        ItemCarrito itemExistente =
                itemCarritoRepository
                        .findByCarritoIdAndInventarioId(
                                carrito.getId(),
                                request.inventarioId()
                        )
                        .orElse(null);

        int cantidadFinal =
                itemExistente == null
                        ? request.cantidad()
                        : itemExistente.getCantidad()
                        + request.cantidad();

        validarStock(
                producto.nombre(),
                inventario.stockActual(),
                cantidadFinal
        );

        BigDecimal descuento =
                promocionService.calcularMejorDescuento(
                        producto.id(),
                        producto.precio(),
                        cantidadFinal
                );

        if (itemExistente != null) {
            actualizarItemExistente(
                    itemExistente,
                    producto,
                    cantidadFinal,
                    descuento
            );
        } else {
            ItemCarrito nuevoItem =
                    crearNuevoItem(
                            carrito,
                            inventario,
                            producto,
                            request.cantidad(),
                            descuento
                    );

            carrito.agregarItem(nuevoItem);
        }

        carrito.recalcularTotales();

        Carrito carritoGuardado =
                carritoRepository.save(carrito);

        return convertirResponse(carritoGuardado);
    }

    /**
     * Actualiza la cantidad de un producto ya agregado.
     */
    @Transactional
    public CarritoResponse actualizarCantidad(
            String clienteEmail,
            Long itemId,
            ActualizarCantidadItemRequest request,
            String authorizationHeader
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        validarAuthorizationHeader(
                authorizationHeader
        );

        ItemCarrito item =
                itemCarritoRepository
                        .findByIdAndCarritoClienteEmail(
                                itemId,
                                emailNormalizado
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Ítem de carrito no encontrado: "
                                                + itemId
                                )
                        );

        Carrito carrito = item.getCarrito();

        validarCarritoActivo(carrito);

        CatalogInventarioResponse inventario =
                catalogClient.obtenerInventario(
                        item.getInventarioId(),
                        authorizationHeader
                );

        validarInventarioSucursal(
                carrito,
                inventario
        );

        if (!inventario.productoId()
                .equals(item.getProductoId())) {

            throw new OperacionInvalidaException(
                    "El producto del inventario no coincide "
                            + "con el producto del carrito"
            );
        }

        validarStock(
                item.getNombreProducto(),
                inventario.stockActual(),
                request.cantidad()
        );

        BigDecimal descuento =
                promocionService.calcularMejorDescuento(
                        item.getProductoId(),
                        item.getPrecioUnitario(),
                        request.cantidad()
                );

        item.setCantidad(request.cantidad());
        item.setDescuento(descuento);
        item.recalcularSubtotal();

        carrito.recalcularTotales();

        Carrito carritoGuardado =
                carritoRepository.save(carrito);

        return convertirResponse(carritoGuardado);
    }

    /**
     * Elimina un producto específico del carrito.
     */
    @Transactional
    public CarritoResponse eliminarItem(
            String clienteEmail,
            Long itemId
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        ItemCarrito item =
                itemCarritoRepository
                        .findByIdAndCarritoClienteEmail(
                                itemId,
                                emailNormalizado
                        )
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Ítem de carrito no encontrado: "
                                                + itemId
                                )
                        );

        Carrito carrito = item.getCarrito();

        validarCarritoActivo(carrito);

        carrito.removerItem(item);
        carrito.recalcularTotales();

        Carrito carritoGuardado =
                carritoRepository.save(carrito);

        return convertirResponse(carritoGuardado);
    }

    /**
     * Elimina todos los productos del carrito activo.
     */
    @Transactional
    public CarritoResponse vaciar(
            String clienteEmail
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        Carrito carrito =
                obtenerCarritoActivo(emailNormalizado);

        List<ItemCarrito> itemsActuales =
                new ArrayList<>(carrito.getItems());

        for (ItemCarrito item : itemsActuales) {
            carrito.removerItem(item);
        }

        carrito.recalcularTotales();

        Carrito carritoGuardado =
                carritoRepository.save(carrito);

        return convertirResponse(carritoGuardado);
    }

    /**
     * Lista los carritos históricos del cliente.
     */
    @Transactional(readOnly = true)
    public List<CarritoResponse> listarHistorial(
            String clienteEmail
    ) {
        String emailNormalizado =
                normalizarEmail(clienteEmail);

        return carritoRepository
                .findByClienteEmailOrderByFechaCreacionDesc(
                        emailNormalizado
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private Carrito obtenerCarritoActivo(
            String clienteEmail
    ) {
        return carritoRepository
                .findByClienteEmailAndEstado(
                        clienteEmail,
                        EstadoCarrito.ACTIVO
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente no posee un carrito activo"
                        )
                );
    }

    private void validarCarritoActivo(
            Carrito carrito
    ) {
        if (carrito.getEstado()
                != EstadoCarrito.ACTIVO) {

            throw new OperacionInvalidaException(
                    "El carrito ya no se encuentra activo"
            );
        }
    }

    private void validarInventarioSucursal(
            Carrito carrito,
            CatalogInventarioResponse inventario
    ) {
        if (inventario.id() == null) {
            throw new OperacionInvalidaException(
                    "El inventario recibido no es válido"
            );
        }

        if (inventario.productoId() == null) {
            throw new OperacionInvalidaException(
                    "El inventario no posee un producto válido"
            );
        }

        if (inventario.sucursalId() == null
                || !inventario.sucursalId()
                .equals(carrito.getSucursalId())) {

            throw new OperacionInvalidaException(
                    "El inventario no pertenece a la "
                            + "sucursal seleccionada en el carrito"
            );
        }
    }

    private void validarProducto(
            CatalogInventarioResponse inventario,
            CatalogProductoResponse producto
    ) {
        if (producto.id() == null
                || !producto.id()
                .equals(inventario.productoId())) {

            throw new OperacionInvalidaException(
                    "El producto no coincide con el inventario"
            );
        }

        if (!Boolean.TRUE.equals(producto.activo())) {
            throw new OperacionInvalidaException(
                    "El producto "
                            + producto.nombre()
                            + " se encuentra inactivo"
            );
        }

        if (producto.precio() == null
                || producto.precio()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new OperacionInvalidaException(
                    "El producto no posee un precio válido"
            );
        }
    }

    private void validarStock(
            String nombreProducto,
            Integer stockActual,
            Integer cantidadSolicitada
    ) {
        int stockDisponible =
                stockActual == null
                        ? 0
                        : stockActual;

        if (cantidadSolicitada > stockDisponible) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para "
                            + nombreProducto
                            + ". Disponible: "
                            + stockDisponible
                            + ", solicitado: "
                            + cantidadSolicitada
            );
        }
    }

    private void actualizarItemExistente(
            ItemCarrito item,
            CatalogProductoResponse producto,
            Integer cantidadFinal,
            BigDecimal descuento
    ) {
        item.setSku(producto.sku());
        item.setNombreProducto(producto.nombre());
        item.setPrecioUnitario(producto.precio());
        item.setCantidad(cantidadFinal);
        item.setDescuento(descuento);
        item.recalcularSubtotal();
    }

    private ItemCarrito crearNuevoItem(
            Carrito carrito,
            CatalogInventarioResponse inventario,
            CatalogProductoResponse producto,
            Integer cantidad,
            BigDecimal descuento
    ) {
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .productoId(producto.id())
                .inventarioId(inventario.id())
                .sku(producto.sku())
                .nombreProducto(producto.nombre())
                .precioUnitario(producto.precio())
                .cantidad(cantidad)
                .descuento(descuento)
                .subtotal(BigDecimal.ZERO)
                .build();

        item.recalcularSubtotal();

        return item;
    }

    private void validarAuthorizationHeader(
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || authorizationHeader.isBlank()
                || !authorizationHeader
                .startsWith("Bearer ")) {

            throw new OperacionInvalidaException(
                    "No fue posible reenviar el JWT "
                            + "a Catalog Service"
            );
        }
    }

    private String normalizarEmail(
            String email
    ) {
        if (email == null || email.isBlank()) {
            throw new OperacionInvalidaException(
                    "No fue posible identificar al usuario"
            );
        }

        return email.trim().toLowerCase();
    }

    private CarritoResponse convertirResponse(
            Carrito carrito
    ) {
        List<ItemCarritoResponse> items =
                carrito.getItems() == null
                        ? List.of()
                        : carrito.getItems()
                        .stream()
                        .map(this::convertirItemResponse)
                        .toList();

        return new CarritoResponse(
                carrito.getId(),
                carrito.getClienteEmail(),
                carrito.getSucursalId(),
                carrito.getEstado(),
                carrito.getSubtotal(),
                carrito.getDescuento(),
                carrito.getTotal(),
                carrito.getFechaCreacion(),
                carrito.getFechaActualizacion(),
                items
        );
    }

    private ItemCarritoResponse convertirItemResponse(
            ItemCarrito item
    ) {
        return new ItemCarritoResponse(
                item.getId(),
                item.getProductoId(),
                item.getInventarioId(),
                item.getSku(),
                item.getNombreProducto(),
                item.getPrecioUnitario(),
                item.getCantidad(),
                item.getDescuento(),
                item.getSubtotal()
        );
    }
}