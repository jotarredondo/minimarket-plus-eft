# MiniMarket Plus - EFT Desarrollo Backend II

Proyecto desarrollado para la Evaluación Final Transversal de la asignatura Desarrollo Backend II.

## Arquitectura

El sistema estará compuesto por tres microservicios:

- `auth-service`: usuarios, roles, autenticación y JWT.
- `catalog-service`: productos, categorías e inventario.
- `sales-service`: carritos y ventas.

## Tecnologías

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- H2 Database
- JUnit 5
- Mockito
- JaCoCo
- OpenAPI y Swagger
- Spring HATEOAS

## Estado del proyecto

| Microservicio | Puerto | Estado |
|---|---:|---|
| Auth Service | 8081 | Implementado |
| Catalog Service | 8082 | Implementado |
| Sales Service | 8083 | En desarrollo |

## Tecnologías utilizadas

- Java 17 o superior
- Spring Boot
- Spring Security
- JSON Web Token
- Spring Data JPA
- H2 Database
- OpenAPI y Swagger UI
- Spring HATEOAS
- JUnit 5
- Mockito
- JaCoCo
- Maven

## Estructura del repositorio

- `auth-service`: registro, autenticación, generación de JWT y gestión de roles.
- `catalog-service`: categorías, productos, sucursales, inventario,
  movimientos de stock y órdenes de reposición.
- `sales-service`: carrito y procesamiento de ventas.
- `postman`: colecciones y entornos utilizados para validar los endpoints.
- `docs`: archivos OpenAPI exportados y evidencias técnicas del proyecto.

## Orden de inicio

Los servicios deben iniciarse en el siguiente orden:

1. `auth-service`, puerto 8081.
2. `catalog-service`, puerto 8082.
3. `sales-service`, puerto 8083.

`auth-service` genera los JWT utilizados para acceder a los endpoints protegidos de los otros microservicios.

## Auth Service

Responsabilidades:

- Registro de clientes.
- Inicio de sesión.
- Generación y validación de JWT.
- Cifrado de contraseñas con BCrypt.
- Gestión de los roles `CLIENTE`, `CAJERO` y `ADMIN`.
- Consulta de la identidad del usuario autenticado.
- Creación inicial de usuarios administrativos configurables mediante variables de entorno.

### Documentación

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Consola H2: `http://localhost:8081/h2-console`

## Catalog Service

Responsabilidades:

- Gestión de categorías.
- Gestión de productos.
- Gestión de sucursales.
- Control de inventario por producto y sucursal.
- Registro de movimientos de entrada, salida y ajuste.
- Salida de inventario originada por una venta.
- Generación automática de órdenes de reposición.
- Consulta y actualización del estado de las órdenes de reposición.
- Navegación de recursos mediante enlaces HATEOAS.

### Recursos principales

- `/api/categorias`
- `/api/productos`
- `/api/sucursales`
- `/api/inventarios`
- `/api/inventarios/{inventarioId}/movimientos`
- `/api/inventarios/{inventarioId}/salidas-venta`
- `/api/ordenes-reposicion`

### Documentación

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Consola H2: `http://localhost:8082/h2-console`

## Sales Service

Responsabilidades:

- Crear o recuperar el carrito activo de un cliente.
- Agregar, actualizar y eliminar productos del carrito.
- Validar inventario, sucursal, producto activo y stock disponible mediante `catalog-service`.
- Aplicar promociones vigentes al carrito.
- Convertir el carrito en un pedido para retiro en tienda o despacho a domicilio.
- Consultar pedidos propios y pedidos pendientes.
- Confirmar ventas exclusivamente con el rol `CAJERO`.
- Conservar el detalle histórico de productos, cantidades, precios y descuentos vendidos.
- Evitar la confirmación duplicada de un pedido.
- Descontar inventario mediante integración HTTP con `catalog-service`.
- Administrar promociones centralizadas.
- Generar reportes de ventas y rotación de productos.

### Recursos principales

#### Carritos

- `POST /api/carritos`
- `GET /api/carritos/actual`
- `GET /api/carritos/actual/hateoas`
- `POST /api/carritos/items`
- `PATCH /api/carritos/items/{itemId}`
- `DELETE /api/carritos/items/{itemId}`
- `DELETE /api/carritos/items`
- `GET /api/carritos`

#### Pedidos

- `POST /api/pedidos`
- `GET /api/pedidos/mis-pedidos`
- `GET /api/pedidos/mis-pedidos/{pedidoId}`
- `GET /api/pedidos/pendientes`
- `GET /api/pedidos/{pedidoId}/gestion`

#### Ventas

- `POST /api/ventas/pedidos/{pedidoId}/confirmar`
- `GET /api/ventas/mis-ventas`
- `GET /api/ventas`
- `GET /api/ventas/{ventaId}`

#### Promociones

- `GET /api/promociones/activas`
- `POST /api/promociones`
- `PATCH /api/promociones/{promocionId}/estado`

#### Reportes

- `GET /api/reportes/resumen-ventas`
- `GET /api/reportes/productos-rotacion`

### Documentación

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`
- Consola H2: `http://localhost:8083/h2-console`

## Roles y permisos

### CLIENTE

Puede:

- Registrarse e iniciar sesión.
- Consultar categorías, productos, sucursales e inventario disponible.
- Crear y administrar su carrito.
- Crear pedidos para retiro o despacho.
- Consultar sus propios pedidos.
- Consultar promociones activas.

### CAJERO

Puede:

- Consultar catálogo e inventario.
- Consultar pedidos pendientes.
- Confirmar una venta.
- Consultar las ventas realizadas por su usuario.
- Consultar promociones activas.

La confirmación de ventas está restringida exclusivamente al rol `CAJERO`.

### ADMIN

Puede:

- Crear y modificar categorías, productos y sucursales.
- Crear inventarios y registrar movimientos de stock.
- Consultar y administrar órdenes de reposición.
- Crear, activar y desactivar promociones.
- Consultar todas las ventas.
- Consultar el resumen de ventas y la rotación de productos.

## Seguridad

- Arquitectura stateless sin sesiones de servidor.
- Autenticación mediante JWT.
- Autorización mediante roles y `@PreAuthorize`.
- Contraseñas almacenadas con BCrypt.
- Respuestas diferenciadas para acceso no autenticado (`401`) y acceso sin permisos (`403`).
- Swagger UI, OpenAPI y consola H2 permitidos sin autenticación para el entorno académico local.
- Los demás endpoints requieren un JWT válido.

## Uso de Swagger UI

1. Iniciar los tres microservicios.
2. Iniciar sesión mediante `auth-service`.
3. Copiar el JWT retornado.
4. Abrir Swagger UI del servicio correspondiente.
5. Seleccionar `Authorize`.
6. Ingresar solamente el token, sin agregar manualmente la palabra `Bearer`.
7. Ejecutar los endpoints permitidos para el rol autenticado.

## OpenAPI

Cada servicio expone su contrato en `/v3/api-docs` y su interfaz Swagger en `/swagger-ui.html`.

La carpeta `open-api` contiene archivos JSON exportados desde la documentación OpenAPI para revisión e importación en Postman.

Actualmente el repositorio incluye el contrato exportado de `catalog-service`.

## HATEOAS

`catalog-service` incorpora enlaces dinámicos en recursos principales como categorías, productos, sucursales, inventarios y movimientos.

`sales-service` dispone de una respuesta HATEOAS para el carrito activo mediante:

```text
GET /api/carritos/actual/hateoas
```

La respuesta incluye enlaces dentro de `_links` para navegar hacia el carrito actual, historial y operaciones relacionadas.

## Integración entre microservicios

`sales-service` consume `catalog-service` para:

- Consultar productos.
- Consultar inventarios.
- Validar la sucursal asociada al carrito o pedido.
- Verificar stock antes de crear el pedido y confirmar la venta.
- Registrar una salida de inventario después de confirmar una venta.

El JWT recibido por `sales-service` se reenvía a `catalog-service`, manteniendo la autenticación y los permisos durante la integración.

## Pruebas y cobertura

Los microservicios incluyen pruebas unitarias desarrolladas con JUnit 5 y Mockito. También se incluyen pruebas de controladores, entidades, reglas de negocio, validaciones y seguridad.

JaCoCo genera el reporte en cada microservicio dentro de:

```text
target/site/jacoco/index.html
```

Cobertura registrada:

| Microservicio |                                                   Cobertura |
|---|------------------------------------------------------------:|
| Auth Service |                                                        81 % |
| Catalog Service |                                                        83 % |
| Sales Service |   80 % en la última ejecución después de incorporar HATEOAS |

Antes de la entrega final se debe ejecutar nuevamente la validación de `sales-service` y actualizar esta tabla con el porcentaje definitivo. El objetivo académico es mantener una cobertura igual o superior al 80 %.

## Postman

La carpeta `postman` contiene la colección utilizada para validar:

- Registro e inicio de sesión.
- Tokens y roles.
- Operaciones del catálogo.
- Control de inventario y reposición.
- Flujo de carrito, pedido y venta.
- Validación de stock insuficiente.
- Acceso denegado según rol.
- Integración entre `sales-service` y `catalog-service`.

Los tokens deben configurarse localmente y eliminarse antes de exportar o subir la colección.

## Base de datos

Los tres microservicios utilizan H2 en memoria para el entorno académico local.

Los datos pueden reiniciarse al detener o reiniciar los servicios, de acuerdo con la configuración de persistencia de cada microservicio.

## Flujo de prueba recomendado

1. Iniciar sesión como `ADMIN`, `CAJERO` y `CLIENTE`.
2. Crear categoría, producto, sucursal e inventario como `ADMIN`.
3. Crear un carrito como `CLIENTE`.
4. Agregar productos y verificar la validación de stock.
5. Crear un pedido pendiente.
6. Intentar confirmar la venta con `CLIENTE` y comprobar el `403`.
7. Confirmar la venta con `CAJERO` y comprobar el `201`.
8. Consultar el inventario y verificar la salida de stock.
9. Repetir la confirmación y comprobar que se evita una venta duplicada.
10. Consultar promociones y reportes según el rol correspondiente.

## Evidencias para la EFT

La entrega debe incluir evidencias de:

- Ejecución de los tres microservicios.
- Inicio de sesión y generación de JWT.
- Acceso permitido y denegado según roles.
- Flujo completo de carrito, pedido y venta.
- Inventario antes y después de confirmar la venta.
- Movimiento de salida generado por la venta.
- Swagger UI y contratos OpenAPI.
- Respuestas con enlaces HATEOAS.
- Colección Postman funcional.
- Ejecución exitosa de pruebas unitarias.
- Reportes JaCoCo.
- Repositorio GitHub sin secretos ni tokens expuestos.

## Autores

Proyecto desarrollado con fines académicos para DUOC UC.