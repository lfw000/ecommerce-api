# E-commerce API

API REST para una plataforma de e-commerce tipo Amazon, construida con **Spring Boot 3**, **Spring Security** y **JWT**. Incluye gestión completa de usuarios, productos, carrito de compras, órdenes y pagos.

## Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Configuración](#-configuración)
- [Base de Datos](#-base-de-datos)
- [Documentación de la API](#-documentación-de-la-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Flujo de Autenticación](#-flujo-de-autenticación)
- [Códigos de Error](#-códigos-de-error)
- [Mejoras Pendientes](#-mejoras-pendientes)
- [Contribución](#-contribución)

## Características

### Autenticación y Usuarios
- Registro y login con JWT
- Roles: `ROLE_USER` y `ROLE_ADMIN`
- Gestión de perfiles de usuario
- Direcciones de envío/facturación (múltiples por usuario)

### Productos y Catálogo
- CRUD completo de productos
- Categorías con jerarquía (subcategorías)
- Atributos variables de productos (JSON)
- Múltiples imágenes por producto
- Búsqueda por nombre, descripción, categoría y precio
- Productos destacados

### Carrito de Compras
- Agregar, actualizar y eliminar productos
- Cálculo automático de subtotales y total
- Carrito persistente por usuario
- Conversión automática a orden

### Órdenes y Pagos
- Creación de órdenes desde el carrito
- Estados de orden: `PENDING` → `PAID` → `PROCESSING` → `SHIPPED` → `DELIVERED`
- Cancelación de órdenes con reembolso automático
- Procesamiento de pagos (simulado)
- Reembolsos totales y parciales

### Seguridad
- Autenticación con JWT
- Protección de endpoints por rol (`@PreAuthorize`)
- Validación de pertenencia de recursos
- Manejo global de excepciones con códigos de error

## 🛠 Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.4.x | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Persistencia |
| PostgreSQL | 15+ | Base de datos |
| Flyway | 10.x | Migraciones |
| MapStruct | 1.5.x | Mapeo entidad-DTO |
| Lombok | 1.18.x | Reducción de boilerplate |
| Jackson | 2.x | Serialización JSON |
| JJWT | 0.12.x | Tokens JWT |
| OpenAPI (Swagger) | 2.6.x | Documentación |
| Maven | 3.9+ | Gestor de dependencias |

## Requisitos Previos

- **Java 21** o superior
- **Maven 3.9** o superior
- **PostgreSQL 15** o superior
- **Git** (opcional, para clonar)

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/lfw000/ecommerce-api
cd ecommerce-api

### 2. Crear base de datos

```bash
psql -U postgres
CREATE DATABASE ecommerce_dev;
\q
```

### 3. Configurar variables de entorno

Crear archivo `.env` en la raíz del proyecto:

```bash
DB_URL=jdbc:postgresql://localhost:5432/ecommerce_dev
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña
JWT_SECRET=tu_secret_key_muy_segura_1234567890
```

### 4. Ejecutar migraciones Flyway

```bash
./mvnw flyway:migrate
```

### 5. Ejecutar la aplicación

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en `http://localhost:8080`

## Configuración

### Perfiles disponibles

| Perfil | Descripción                          |
|--------|--------------------------------------|
| `dev`  | Desarrollo local (logs detallados)   |
| `prod` | Producción (logs mínimos, CORS cerrado) |
| `test` | Pruebas (base de datos H2)           |

### Configuración JWT

```yaml
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 900000
    refresh-expiration-ms: 604800000
```

## Base de Datos

### Comandos útiles de Flyway

```bash
./mvnw flyway:migrate   # Migrar a última versión
./mvnw flyway:info      # Ver estado actual
./mvnw flyway:clean     # Limpiar BD (solo desarrollo)
./mvnw flyway:repair    # Reparar historial
```

## Documentación de la API

### Acceso a Swagger UI

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Endpoints Principales

#### Autenticación

| Método | Endpoint | Descripción | Rol        |
|--------|----------|-------------|------------|
| `POST` | `/api/auth/register` | Registro    | Público    |
| `POST` | `/api/auth/login`    | Login       | Público    |
| `POST` | `/api/auth/refresh`  | Renovar token | Público  |
| `POST` | `/api/auth/logout`   | Cerrar sesión | Autenticado |

#### Usuarios

| Método | Endpoint | Descripción | Rol        |
|--------|----------|-------------|------------|
| `GET`  | `/api/users/me` | Perfil actual | USER |
| `PUT`  | `/api/users/me` | Actualizar perfil | USER |
| `GET`  | `/api/users/me/addresses` | Listar direcciones | USER |
| `POST` | `/api/users/me/addresses` | Agregar dirección | USER |
| `GET`  | `/api/users` | Listar usuarios | ADMIN |
| `GET`  | `/api/users/{id}` | Obtener usuario | ADMIN |

#### Productos

| Método | Endpoint | Descripción | Rol        |
|--------|----------|-------------|------------|
| `GET`  | `/api/products` | Listar productos | Público |
| `GET`  | `/api/products/{id}` | Obtener producto | Público |
| `GET`  | `/api/products/search` | Buscar productos | Público |
| `POST` | `/api/products` | Crear producto | ADMIN |
| `PUT`  | `/api/products/{id}` | Actualizar producto | ADMIN |
| `DELETE` | `/api/products/{id}` | Eliminar producto | ADMIN |

#### Carrito

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| `GET`  | `/api/cart` | Ver carrito | USER |
| `POST` | `/api/cart/items` | Agregar producto | USER |
| `PUT`  | `/api/cart/items` | Actualizar cantidad | USER |
| `DELETE` | `/api/cart/items/{productId}` | Eliminar producto | USER |
| `DELETE` | `/api/cart` | Vaciar carrito | USER |

#### Órdenes

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| `POST` | `/api/orders` | Crear orden | USER |
| `GET`  | `/api/orders` | Listar órdenes | USER |
| `GET`  | `/api/orders/{id}` | Obtener orden | USER |
| `POST` | `/api/orders/{id}/cancel` | Cancelar orden | USER |
| `POST` | `/api/orders/admin/{id}/ship` | Marcar enviada | ADMIN |
| `POST` | `/api/orders/admin/{id}/deliver` | Marcar entregada | ADMIN |

#### Pagos

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| `POST` | `/api/payments/order/{orderId}/process` | Procesar pago | USER |
| `GET`  | `/api/payments/order/{orderId}` | Ver pago | USER |
| `POST` | `/api/payments/{id}/refund` | Reembolsar | ADMIN |

### Ejemplos de Requests

#### Registrar usuario

```json
POST /api/auth/register
{
    "email": "usuario@example.com",
    "password": "password123",
    "firstName": "Juan",
    "lastName": "Pérez"
}
```

#### Login

```json
POST /api/auth/login
{
    "email": "usuario@example.com",
    "password": "password123"
}
```

#### Crear orden

```json
POST /api/orders
Authorization: Bearer {token}
{
    "shippingAddressId": 1,
    "billingAddressId": 1,
    "shippingMethod": "standard"
}
```

## Estructura del Proyecto

```
src/main/java/com/ecommerce/
├── api/                 # Controladores, DTOs, excepciones
│   ├── controller/
│   ├── dto/
│   └── exception/
├── domain/              # Entidades de dominio
│   ├── cart/
│   ├── order/
│   ├── payment/
│   ├── product/
│   └── user/
├── infrastructure/      # Configuraciones
├── security/            # JWT, UserDetails
├── services/            # Lógica de negocio
├── mappers/             # MapStruct
└── shared/              # Clases compartidas (enums, excepciones base)
```
## Códigos de Error

| errorCode | HTTP | Descripción |
|-----------|------|-------------|
| `RESOURCE_NOT_FOUND` | 404 | Recurso no encontrado |
| `DUPLICATE_RESOURCE` | 409 | Recurso duplicado |
| `UNAUTHORIZED_ACCESS` | 403 | Sin permiso para el recurso |
| `ACCESS_DENIED` | 403 | Rol insuficiente |
| `INVALID_CREDENTIALS` | 401 | Credenciales incorrectas |
| `INSUFFICIENT_STOCK` | 400 | Stock insuficiente |
| `INVALID_STATE` | 400 | Estado inválido para la acción |
| `EMPTY_CART` | 400 | Carrito vacío |
| `VALIDATION_ERROR` | 400 | Error de validación |
| `JWT_EXPIRED` | 401 | Token expirado |
| `DATABASE_ERROR` | 500 | Error de base de datos |
| `PAYMENT_GATEWAY_ERROR` | 502 | Error en pasarela de pagos |

## Licencia

MIT License

## Autor

Luis Patal - [@lfw000](https://github.com/lfw000)

---