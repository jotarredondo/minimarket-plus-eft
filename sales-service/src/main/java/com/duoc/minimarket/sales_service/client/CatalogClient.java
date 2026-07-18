package com.duoc.minimarket.sales_service.client;

import com.duoc.minimarket.sales_service.dto.catalog.CatalogInventarioResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogMovimientoResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogProductoResponse;
import com.duoc.minimarket.sales_service.dto.catalog.CatalogSalidaVentaRequest;
import com.duoc.minimarket.sales_service.exception.IntegracionCatalogoException;
import com.duoc.minimarket.sales_service.exception.RecursoNoEncontradoException;
import com.duoc.minimarket.sales_service.exception.StockInsuficienteException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    public CatalogProductoResponse obtenerProducto(
            Long productoId,
            String authorizationHeader
    ) {
        try {
            CatalogProductoResponse producto =
                    catalogRestClient
                            .get()
                            .uri(
                                    "/api/productos/{productoId}",
                                    productoId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(CatalogProductoResponse.class);

            if (producto == null) {
                throw new IntegracionCatalogoException(
                        "Catalog Service respondió sin información del producto"
                );
            }

            return producto;

        } catch (HttpClientErrorException.NotFound exception) {
            throw new RecursoNoEncontradoException(
                    "Producto no encontrado en Catalog Service: "
                            + productoId
            );

        } catch (RestClientResponseException exception) {
            throw new IntegracionCatalogoException(
                    "Catalog Service rechazó la consulta del producto",
                    exception
            );

        } catch (ResourceAccessException exception) {
            throw new IntegracionCatalogoException(
                    "No fue posible conectar con Catalog Service",
                    exception
            );
        }
    }

    public CatalogInventarioResponse obtenerInventario(
            Long inventarioId,
            String authorizationHeader
    ) {
        try {
            CatalogInventarioResponse inventario =
                    catalogRestClient
                            .get()
                            .uri(
                                    "/api/inventarios/{inventarioId}",
                                    inventarioId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(CatalogInventarioResponse.class);

            if (inventario == null) {
                throw new IntegracionCatalogoException(
                        "Catalog Service respondió sin información del inventario"
                );
            }

            return inventario;

        } catch (HttpClientErrorException.NotFound exception) {
            throw new RecursoNoEncontradoException(
                    "Inventario no encontrado en Catalog Service: "
                            + inventarioId
            );

        } catch (RestClientResponseException exception) {
            throw new IntegracionCatalogoException(
                    "Catalog Service rechazó la consulta del inventario",
                    exception
            );

        } catch (ResourceAccessException exception) {
            throw new IntegracionCatalogoException(
                    "No fue posible conectar con Catalog Service",
                    exception
            );
        }
    }

    public CatalogMovimientoResponse registrarSalidaVenta(
            Long inventarioId,
            CatalogSalidaVentaRequest request,
            String authorizationHeader
    ) {
        try {
            CatalogMovimientoResponse movimiento =
                    catalogRestClient
                            .post()
                            .uri(
                                    "/api/inventarios/{inventarioId}/salidas-venta",
                                    inventarioId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .body(request)
                            .retrieve()
                            .body(CatalogMovimientoResponse.class);

            if (movimiento == null) {
                throw new IntegracionCatalogoException(
                        "Catalog Service no confirmó la salida de inventario"
                );
            }

            return movimiento;

        } catch (HttpClientErrorException.NotFound exception) {
            throw new RecursoNoEncontradoException(
                    "Inventario no encontrado en Catalog Service: "
                            + inventarioId
            );

        } catch (HttpClientErrorException.BadRequest exception) {
            throw new StockInsuficienteException(
                    "Catalog Service rechazó la venta por stock insuficiente"
            );

        } catch (RestClientResponseException exception) {
            throw new IntegracionCatalogoException(
                    "Catalog Service rechazó el movimiento de inventario",
                    exception
            );

        } catch (ResourceAccessException exception) {
            throw new IntegracionCatalogoException(
                    "No fue posible conectar con Catalog Service",
                    exception
            );
        }
    }
}
