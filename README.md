# Sistema de Gestión de Cuentas Bancarias

Aplicación full-stack para la gestión de clientes, cuentas bancarias y movimientos financieros.

## 🛠️ Tecnologías Utilizadas

### Backend
- Java 21
- Spring Boot 3.5.9
- Spring Data JPA
- MySQL 8.0
- Maven
- Swagger/OpenAPI 3.0
- JUnit 5 + Mockito

### Frontend
- Angular 21
- TypeScript
- SCSS (sin frameworks CSS)
- Jest para testing
- RxJS

## 📋 Requisitos Previos

- JDK 21
- Node.js 18+ y npm
- Docker y Docker Compose
- MySQL 8.0 (solo para ejecución local sin Docker)

## 🚀 Ejecución Local

### Backend

1. Configurar base de datos MySQL:
```bash
mysql -u root -p
CREATE DATABASE accounts_db;
```

2. Ejecutar el script de base de datos:
```bash
mysql -u root -p accounts_db < entregables/BaseDatos.sql
```

3. Compilar y ejecutar:
```bash
cd backend/accounts-backend
mvn clean install
mvn spring-boot:run
```

El backend estará disponible en `http://localhost:8081`

Swagger UI: `http://localhost:8081/swagger-ui.html`

### Frontend

1. Instalar dependencias:
```bash
cd frontend/accounts-app-frontend
npm install
```

2. Ejecutar en modo desarrollo:
```bash
npm start
```

La aplicación estará disponible en `http://localhost:4200`

## 🐳 Ejecución con Docker

1. Crear archivo `.env` en la raíz del proyecto:
```env
DATASOURCE_PASSWORD=root
DATASOURCE_USERNAME=accounts_db
DB_PORT=3306
SERVER_PORT=8081
FRONTEND_PORT=4200
BASE_IMAGE_TAG=1.0.0
FRONTEND_VERSION=latest
```

2. Levantar todos los servicios:
```bash
docker-compose up -d
```

3. Verificar que los servicios estén corriendo:
```bash
docker-compose ps
```

Servicios disponibles:
- Backend: `http://localhost:8081`
- Frontend: `http://localhost:4200`
- MySQL: `localhost:3306`

## 🧪 Ejecutar Tests

### Backend
```bash
cd backend/accounts-backend
mvn test
```

### Frontend
```bash
cd frontend/accounts-app-frontend
npm test
```

## 📁 Estructura del Proyecto

```
accounts-app/
├── backend/
│   └── accounts-backend/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/core/bank/
│       │   │   │   ├── application/      # Servicios y lógica de negocio
│       │   │   │   ├── domain/           # Entidades y repositorios
│       │   │   │   └── infrastructure/   # Controladores y configuración
│       │   │   └── resources/
│       │   │       ├── openapi.yaml      # Especificación OpenAPI
│       │   │       └── application.properties
│       │   └── test/                     # Tests unitarios
│       └── pom.xml
├── frontend/
│   └── accounts-app-frontend/
│       ├── src/
│       │   └── app/
│       │       ├── layout/               # Componentes de layout
│       │       ├── pages/                # Páginas principales
│       │       └── services/             # Servicios HTTP
│       ├── jest.config.js
│       └── package.json
├── entregables/
│   ├── BaseDatos.sql                     # Script de base de datos
│   └── Accounts Backend API - Casos de Prueba V2.postman_collection.json.json           # Colección de Postman
└── docker-compose.yml
```

## 🎯 Funcionalidades Implementadas

### Backend

✅ CRUD completo de Clientes, Cuentas y Movimientos
✅ Herencia de entidades (Persona → Cliente)
✅ Validaciones de negocio:
  - Saldo no disponible
  - Límite diario de retiro ($1000)
  - Cupo diario excedido
✅ Generación de reportes en JSON y PDF
✅ Manejo de excepciones centralizado
✅ Documentación con Swagger
✅ 30 tests unitarios

### Frontend

✅ CRUD visual para todas las entidades
✅ Búsqueda rápida en tablas
✅ Generación y descarga de reportes PDF
✅ Validaciones en formularios
✅ Notificaciones de éxito/error
✅ Diseño responsive sin frameworks CSS
✅ 19 tests unitarios con Jest

## 🏗️ Patrones y Buenas Prácticas

- **Repository Pattern**: Separación de lógica de acceso a datos
- **Strategy Pattern**: Manejo de tipos de transacciones (Crédito/Débito)
- **DTO Pattern**: Transferencia de datos entre capas
- **Service Layer**: Lógica de negocio centralizada
- **Exception Handling**: Manejo global de excepciones
- **Clean Code**: Código limpio y mantenible
- **SOLID Principles**: Aplicados en toda la arquitectura

## 📝 Endpoints Principales

### Clientes
- `GET /api/clientes` - Listar clientes
- `POST /api/clientes` - Crear cliente
- `GET /api/clientes/{id}` - Obtener cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `PATCH /api/clientes/{id}` - Actualizar parcialmente
- `DELETE /api/clientes/{id}` - Eliminar cliente

### Cuentas
- `GET /api/cuentas` - Listar cuentas
- `POST /api/cuentas` - Crear cuenta
- `GET /api/cuentas/{id}` - Obtener cuenta
- `PATCH /api/cuentas/{id}` - Actualizar cuenta
- `DELETE /api/cuentas/{id}` - Eliminar cuenta

### Movimientos
- `GET /api/movimientos` - Listar movimientos
- `POST /api/movimientos` - Registrar movimiento
- `DELETE /api/movimientos?id={id}` - Anular movimiento

### Reportes
- `GET /api/reportes?clienteId={id}&from={fecha}&to={fecha}&format={json|pdf}` - Generar reporte

## 📮 Colección de Postman

Importar el archivo `entregables/Accounts Backend API - Casos de Prueba V2.postman_collection.json.json` en Postman para probar todos los endpoints.

## 🔧 Configuración

Las configuraciones se pueden modificar en:
- Backend: `backend/accounts-backend/src/main/resources/application.properties`
- Frontend: `frontend/accounts-app-frontend/src/environments/`
- Docker: `.env` en la raíz del proyecto

---

Desarrollado por Jefferson Noroña
