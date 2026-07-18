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

1. Auth Service, puerto 8081.
2. Catalog Service, puerto 8082.
3. Sales Service, puerto 8083.

Auth Service debe estar disponible antes de probar los servicios protegidos,
ya que es el encargado de generar los JWT.

## Auth Service

Responsabilidades:

- Registro de clientes.
- Inicio de sesión.
- Generación y validación de JWT.
- Asignación de los roles CLIENTE, CAJERO y ADMIN.
- Consulta del usuario autenticado.

### Documentación

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Catalog Service

Responsabilidades:

- Gestión de categorías.
- Gestión de productos.
- Gestión de sucursales.
- Control de inventario por producto y sucursal.
- Registro de entradas, salidas y ajustes de stock.
- Generación automática de órdenes de reposición.
- Navegación de recursos mediante HATEOAS.

### Documentación

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Consola H2: `http://localhost:8082/h2-console`

## Roles y permisos

### CLIENTE

Puede consultar:

- Categorías.
- Productos.
- Sucursales.
- Disponibilidad de inventario.
- Información relacionada con su carrito.

### CAJERO

Puede consultar:

- Categorías.
- Productos.
- Sucursales.
- Inventarios.
- Información necesaria para procesar ventas.

### ADMIN

Puede:

- Crear y modificar categorías.
- Crear y modificar productos.
- Crear y modificar sucursales.
- Crear inventarios.
- Registrar movimientos de stock.
- Consultar y administrar órdenes de reposición.
- Consultar reportes administrativos.

## Uso de Swagger UI

1. Iniciar Auth Service y Catalog Service.
2. Iniciar sesión mediante Auth Service.
3. Copiar el JWT obtenido.
4. Abrir Swagger UI del servicio que se desea probar.
5. Seleccionar el botón `Authorize`.
6. Ingresar solamente el JWT, sin agregar manualmente la palabra `Bearer`.
7. Ejecutar los endpoints según el rol del usuario autenticado.

Los JWT no deben aparecer completos en capturas, documentos o archivos
almacenados en el repositorio.

## HATEOAS

Los recursos principales incluyen enlaces dinámicos dentro de `_links`.

Entre las relaciones disponibles se encuentran:

- Producto hacia su categoría e inventarios.
- Categoría hacia sus productos.
- Sucursal hacia sus inventarios.
- Inventario hacia producto, sucursal, movimientos y órdenes de reposición.
- Movimiento hacia el inventario relacionado.

Las respuestas de colecciones pueden incluir los elementos dentro de
`_embedded`.

## Pruebas y cobertura

Los microservicios incluyen pruebas unitarias desarrolladas con JUnit 5 y
Mockito.

JaCoCo genera el reporte de cobertura al ejecutar la validación Maven del
proyecto.

Cobertura actual:

| Microservicio | Cobertura |
|---|---:|
| Auth Service | 81 % |
| Catalog Service | 84 % |

El reporte de cada servicio se genera dentro de:

`target/site/jacoco/index.html`

## Postman

La carpeta `postman` contiene:

- La colección funcional de MiniMarket Plus.
- El entorno con las URL locales.
- La colección generada desde OpenAPI.
- Solicitudes de validación para los roles CLIENTE, CAJERO y ADMIN.

Los tokens deben configurarse localmente y no deben almacenarse con valores
reales en los archivos exportados.

## Archivos OpenAPI

Los archivos JSON exportados desde `/v3/api-docs` se almacenan en la carpeta
`docs/openapi`.

Estos archivos permiten:

- Revisar el contrato de los servicios.
- Importar la API en Postman.
- Validar la consistencia entre documentación e implementación.

## Base de datos

Actualmente se utiliza H2 para el entorno académico y local.

La información almacenada puede reiniciarse cada vez que se detiene o
reinicia el microservicio, dependiendo de la configuración utilizada.

## Seguridad

- La autenticación se realiza mediante JWT.
- Las APIs no mantienen sesiones de usuario.
- Los permisos se controlan mediante roles.
- Las contraseñas se almacenan cifradas con BCrypt.
- Los secretos y tokens no deben subirse a GitHub.

## Evidencias para la EFT

Cada integrante debe documentar:

- Ejecución de los microservicios.
- Inicio de sesión y generación de JWT.
- Acceso permitido y denegado según los roles.
- Swagger UI con todos los endpoints documentados.
- Respuestas que contengan enlaces HATEOAS.
- Archivo JSON generado desde `/v3/api-docs`.
- Importación y prueba de OpenAPI en Postman.
- Ejecución exitosa de las pruebas unitarias.
- Reportes de cobertura JaCoCo.
- Integración entre los microservicios.
